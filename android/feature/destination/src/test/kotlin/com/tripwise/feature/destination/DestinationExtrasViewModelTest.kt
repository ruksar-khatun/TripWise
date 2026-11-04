package com.tripwise.feature.destination

import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.model.ActivityCategory
import com.tripwise.core.model.Attraction
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.GeoPoint
import com.tripwise.core.model.Money
import com.tripwise.core.model.NearbyPlace
import com.tripwise.core.testing.data.TestData
import com.tripwise.core.testing.fakes.FakeDestinationRepository
import com.tripwise.core.testing.fakes.FakePlacesRepository
import com.tripwise.core.testing.rules.MainDispatcherRule
import com.tripwise.core.testing.rules.collectInBackground
import com.tripwise.feature.destination.extras.DestinationExtrasAction
import com.tripwise.feature.destination.extras.DestinationExtrasViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DestinationExtrasViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private fun attraction(id: String, c: ActivityCategory) =
        Attraction(id, "kyoto", id, c, GeoPoint(0.0, 0.0), 60, Money(0, CurrencyCode("USD")), "d")
    private val kyoto = TestData.destination("kyoto").copy(
        attractions = listOf(attraction("a", ActivityCategory.SIGHTSEEING), attraction("b", ActivityCategory.FOOD), attraction("c", ActivityCategory.SIGHTSEEING)),
    )

    @Test fun `categories come from attractions and nearby loads only on request`() = runTest {
        val places = FakePlacesRepository(Outcome.Success(listOf(NearbyPlace("p", "Temple", GeoPoint(0.0, 0.0), 4.5, 10, null, null, null))))
        val vm = DestinationExtrasViewModel("kyoto", FakeDestinationRepository(listOf(kyoto)), places)
        collectInBackground(vm.uiState)
        assertEquals(listOf(ActivityCategory.SIGHTSEEING, ActivityCategory.FOOD), vm.uiState.value.categories)
        assertEquals(null, vm.uiState.value.nearby)
        assertEquals(0, places.calls)

        vm.onAction(DestinationExtrasAction.LoadNearby)
        vm.onAction(DestinationExtrasAction.LoadNearby) // no duplicate call once loaded
        assertEquals(1, places.calls)
        assertEquals("Temple", (vm.uiState.value.nearby as LoadState.Ready).data.single().name)
    }

    @Test fun `places not configured surfaces as an error state`() = runTest {
        val vm = DestinationExtrasViewModel("kyoto", FakeDestinationRepository(listOf(kyoto)), FakePlacesRepository(Outcome.Failure(AppError.NotConfigured)))
        collectInBackground(vm.uiState)
        vm.onAction(DestinationExtrasAction.LoadNearby)
        assertEquals(LoadState.Failed(AppError.NotConfigured), vm.uiState.value.nearby)
    }
}
