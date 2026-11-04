package com.tripwise.feature.trips

import androidx.lifecycle.SavedStateHandle
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.model.ActivityCategory
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.Money
import com.tripwise.core.model.ItineraryItem
import com.tripwise.core.testing.data.TestData
import com.tripwise.core.testing.fakes.FakeDestinationRepository
import com.tripwise.core.testing.fakes.FakeTripRepository
import com.tripwise.core.testing.fakes.FakeUserRepository
import com.tripwise.core.testing.rules.MainDispatcherRule
import com.tripwise.core.testing.rules.collectInBackground
import com.tripwise.feature.trips.create.CreateTripAction
import com.tripwise.feature.trips.create.CreateTripUiState
import com.tripwise.feature.trips.create.CreateTripViewModel
import com.tripwise.feature.trips.create.FieldError
import com.tripwise.feature.trips.detail.TripDetailAction
import com.tripwise.feature.trips.detail.TripDetailUiState
import com.tripwise.feature.trips.detail.TripDetailViewModel
import com.tripwise.feature.trips.list.TripsAction
import com.tripwise.feature.trips.list.TripsTab
import com.tripwise.feature.trips.list.TripsViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

class TripsViewModelsTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 3, 2)
    private val clock = Clock.fixed(Instant.parse("2026-03-02T10:00:00Z"), ZoneOffset.UTC)
    private val past = TestData.trip("past", today.minusDays(20))
    private val ongoing = TestData.trip("ongoing", today.minusDays(1))
    private val future = TestData.trip("future", today.plusDays(10), days = 3)

    @Test fun `upcoming includes ongoing, past tab has past only`() = runTest {
        val vm = TripsViewModel(FakeTripRepository(listOf(future, past, ongoing)), clock)
        collectInBackground(vm.uiState)
        assertEquals(listOf("ongoing", "future"), (vm.uiState.value.trips as LoadState.Ready).data.map { it.trip.id })
        vm.onAction(TripsAction.TabSelected(TripsTab.PAST))
        assertEquals(listOf("past"), (vm.uiState.value.trips as LoadState.Ready).data.map { it.trip.id })
    }

    @Test fun `no trips is an empty ready list, not loading`() = runTest {
        val vm = TripsViewModel(FakeTripRepository(emptyList()), clock)
        collectInBackground(vm.uiState)
        assertEquals(LoadState.Ready(emptyList<Any>()), vm.uiState.value.trips)
    }

    @Test fun `detail builds one entry per day and toggles done`() = runTest {
        val item = ItineraryItem("i1", "future", future.startDate.plusDays(1), LocalTime.NOON, "Museum", ActivityCategory.SIGHTSEEING, null, false)
        val repo = FakeTripRepository(listOf(future), listOf(item))
        val vm = TripDetailViewModel(SavedStateHandle(mapOf("tripId" to "future")), repo, clock)
        collectInBackground(vm.uiState)
        val ready = vm.uiState.value as TripDetailUiState.Ready
        assertEquals(listOf(0, 1, 0), ready.days.map { it.items.size })
        assertEquals("i1", ready.nextItem?.id)

        vm.onAction(TripDetailAction.SetItemDone("i1", true))
        assertTrue((vm.uiState.value as TripDetailUiState.Ready).days[1].items.single().isDone)
    }

    @Test fun `unknown trip is NotFound`() = runTest {
        val vm = TripDetailViewModel(SavedStateHandle(mapOf("tripId" to "x")), FakeTripRepository(listOf(future)), clock)
        collectInBackground(vm.uiState)
        assertEquals(TripDetailUiState.NotFound, vm.uiState.value)
    }

    @Test fun `create form validates only after submit and prefills destination`() = runTest {
        val bali = TestData.destination("bali", name = "Bali")
        val trips = FakeTripRepository()
        val vm = CreateTripViewModel(
            SavedStateHandle(mapOf("destinationId" to "bali")),
            FakeDestinationRepository(listOf(bali)),
            FakeUserRepository(TestData.profile),
            trips,
        )
        assertEquals("Bali", vm.uiState.value.destination)
        assertEquals("INR", vm.uiState.value.currency.code)
        assertEquals(null, vm.uiState.value.visibleError(CreateTripUiState.FIELD_TITLE))

        vm.onAction(CreateTripAction.BudgetChanged("12a3"))
        assertEquals("123", vm.uiState.value.budget)
        vm.onAction(CreateTripAction.Submit)
        assertEquals(FieldError.REQUIRED, vm.uiState.value.visibleError(CreateTripUiState.FIELD_TITLE))
        assertFalse(vm.uiState.value.isValid)

        vm.onAction(CreateTripAction.TitleChanged("Bali week"))
        vm.onAction(CreateTripAction.DatesChanged(today, today.plusDays(6)))
        vm.onAction(CreateTripAction.TravellersChanged(99))
        assertEquals(CreateTripUiState.MAX_TRAVELLERS, vm.uiState.value.travellers)
        assertTrue(vm.uiState.value.isValid)
        assertTrue(trips.created.isEmpty()) // invalid submit above didn't save

        vm.onAction(CreateTripAction.Submit)
        assertEquals("trip-1", vm.uiState.value.savedTripId)
        val saved = trips.created.single()
        assertEquals("bali", saved.destinationId)
        // ₹123 → 12,300 paise
        assertEquals(Money(12_300, CurrencyCode("INR")), saved.budget)
    }

    @Test fun `whole units convert by currency fraction digits`() {
        assertEquals(Money(5_000, CurrencyCode("JPY")), CreateTripViewModel.toMoney(5_000, CurrencyCode("JPY")))
        assertEquals(Money(5_000, CurrencyCode("USD")), CreateTripViewModel.toMoney(50, CurrencyCode("USD")))
    }

    @Test fun `deleting from detail removes the trip`() = runTest {
        val repo = FakeTripRepository(listOf(future))
        val vm = TripDetailViewModel(SavedStateHandle(mapOf("tripId" to "future")), repo, clock)
        collectInBackground(vm.uiState)
        vm.onAction(TripDetailAction.Delete)
        assertEquals(TripDetailUiState.NotFound, vm.uiState.value)
    }
}
