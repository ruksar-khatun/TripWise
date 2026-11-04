package com.tripwise.feature.itinerary

import androidx.lifecycle.SavedStateHandle
import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.model.ActivityCategory
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.ItineraryPlan
import com.tripwise.core.model.Money
import com.tripwise.core.model.PlanDay
import com.tripwise.core.model.PlanItem
import com.tripwise.core.model.PlanSource
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.model.TravelPace
import com.tripwise.core.testing.data.TestData
import com.tripwise.core.testing.fakes.FakeDestinationRepository
import com.tripwise.core.testing.fakes.FakeItineraryPlanRepository
import com.tripwise.core.testing.fakes.FakeTripRepository
import com.tripwise.core.testing.rules.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

class PlannerViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val clock = Clock.fixed(Instant.parse("2026-03-01T10:00:00Z"), ZoneOffset.UTC)
    private val kyoto = TestData.destination("kyoto", name = "Kyoto")
    private fun item(t: String) = PlanItem(LocalTime.parse(t), "Temple", ActivityCategory.SIGHTSEEING, 60, Money(500, CurrencyCode("USD")), null, null)
    private val plan = ItineraryPlan("kyoto", "2 days in Kyoto", "s", PlanSource.AI,
        listOf(PlanDay(1, "Temples", listOf(item("09:00"), item("13:00"))), PlanDay(2, "Food", listOf(item("10:00")))))

    @Test fun `generates with chosen preferences, then saves as a new trip with dated items`() = runTest {
        val planner = FakeItineraryPlanRepository(Outcome.Success(plan)).apply { gate = CompletableDeferred() }
        val trips = FakeTripRepository()
        val vm = PlannerViewModel(SavedStateHandle(mapOf("destinationId" to "kyoto")), FakeDestinationRepository(listOf(kyoto)), planner, trips, clock)
        vm.onAction(PlannerAction.SetDays(2))
        vm.onAction(PlannerAction.SetPace(TravelPace.RELAXED))
        vm.onAction(PlannerAction.ToggleInterest(TravelInterest.FOOD))
        vm.onAction(PlannerAction.Generate)
        assertEquals(LoadState.Loading, vm.uiState.value.plan)
        planner.gate!!.complete(Unit)
        assertEquals(LoadState.Ready(plan), vm.uiState.value.plan)
        with(planner.requests.single()) {
            assertEquals(2, days); assertEquals(TravelPace.RELAXED, pace); assertEquals(setOf(TravelInterest.FOOD), interests)
        }

        vm.onAction(PlannerAction.Save)
        val tripId = vm.uiState.value.savedTripId!!
        val created = trips.created.single()
        assertEquals(LocalDate.of(2026, 3, 31), created.startDate) // default: today + 30
        assertEquals(LocalDate.of(2026, 4, 1), created.endDate)
        val saved = trips.items.value.filter { it.tripId == tripId }
        assertEquals(listOf(LocalDate.of(2026, 3, 31), LocalDate.of(2026, 3, 31), LocalDate.of(2026, 4, 1)), saved.map { it.date })
    }

    @Test fun `planning for an existing trip uses its dates and adds items to it`() = runTest {
        val trip = TestData.trip("t1", LocalDate.of(2026, 5, 10), days = 2, destinationId = "kyoto")
        val trips = FakeTripRepository(listOf(trip))
        val vm = PlannerViewModel(SavedStateHandle(mapOf("tripId" to "t1")), FakeDestinationRepository(listOf(kyoto)), FakeItineraryPlanRepository(Outcome.Success(plan)), trips, clock)
        assertEquals(2, vm.uiState.value.days)
        assertEquals("kyoto", vm.uiState.value.destinationId)
        vm.onAction(PlannerAction.SetDays(9)) // locked to the trip's length
        assertEquals(2, vm.uiState.value.days)
        vm.onAction(PlannerAction.Generate)
        vm.onAction(PlannerAction.Save)
        assertEquals("t1", vm.uiState.value.savedTripId)
        assertTrue(trips.created.isEmpty())
        assertEquals(3, trips.items.value.count { it.tripId == "t1" })
    }

    @Test fun `failure is shown and generation can be retried`() = runTest {
        val planner = FakeItineraryPlanRepository(Outcome.Failure(AppError.Network))
        val vm = PlannerViewModel(SavedStateHandle(mapOf("destinationId" to "kyoto")), FakeDestinationRepository(listOf(kyoto)), planner, FakeTripRepository(), clock)
        vm.onAction(PlannerAction.Generate)
        assertEquals(LoadState.Failed(AppError.Network), vm.uiState.value.plan)
        planner.result = Outcome.Success(plan)
        vm.onAction(PlannerAction.Generate)
        assertEquals(LoadState.Ready(plan), vm.uiState.value.plan)
    }
}
