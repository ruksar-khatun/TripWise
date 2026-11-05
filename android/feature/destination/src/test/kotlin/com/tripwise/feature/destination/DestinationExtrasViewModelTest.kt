package com.tripwise.feature.destination

import com.tripwise.core.common.result.LoadState
import com.tripwise.core.model.ActivityCategory
import com.tripwise.core.model.Attraction
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.GeoPoint
import com.tripwise.core.model.Money
import com.tripwise.core.testing.data.TestData
import com.tripwise.core.testing.fakes.FakeDestinationRepository
import com.tripwise.core.testing.rules.MainDispatcherRule
import com.tripwise.core.testing.rules.collectInBackground
import com.tripwise.feature.destination.extras.DestinationExtrasAction
import com.tripwise.feature.destination.extras.DestinationExtrasViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class DestinationExtrasViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private fun attraction(id: String, c: ActivityCategory) =
        Attraction(id, "jaipur", id, c, GeoPoint(0.0, 0.0), 60, Money(0, CurrencyCode("INR")), "d")
    private val jaipur = TestData.destination("jaipur").copy(
        attractions = listOf(attraction("a", ActivityCategory.SIGHTSEEING), attraction("b", ActivityCategory.FOOD), attraction("c", ActivityCategory.SIGHTSEEING)),
    )

    @Test fun `categories come from attractions and the filter toggles`() = runTest {
        val vm = DestinationExtrasViewModel("jaipur", FakeDestinationRepository(listOf(jaipur)))
        collectInBackground(vm.uiState)
        assertEquals(listOf(ActivityCategory.SIGHTSEEING, ActivityCategory.FOOD), vm.uiState.value.categories)
        vm.onAction(DestinationExtrasAction.SelectCategory(ActivityCategory.FOOD))
        assertEquals(ActivityCategory.FOOD, vm.uiState.value.category)
        vm.onAction(DestinationExtrasAction.SelectCategory(null))
        assertNull(vm.uiState.value.category)
    }

    @Test fun `unknown destination is ready with null`() = runTest {
        val vm = DestinationExtrasViewModel("atlantis", FakeDestinationRepository(listOf(jaipur)))
        collectInBackground(vm.uiState)
        assertEquals(LoadState.Ready(null), vm.uiState.value.destination)
    }
}
