package com.tripwise.feature.home

import com.tripwise.core.common.result.LoadState
import com.tripwise.core.domain.usecase.ObserveSaveableDestinationsUseCase
import com.tripwise.core.testing.data.TestData
import com.tripwise.core.testing.fakes.FakeDestinationRepository
import com.tripwise.core.testing.fakes.FakeSavedDestinationRepository
import com.tripwise.core.testing.fakes.FakeTripRepository
import com.tripwise.core.testing.rules.MainDispatcherRule
import com.tripwise.core.testing.rules.collectInBackground
import kotlinx.coroutines.flow.first
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

class HomeViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 3, 2)
    private val clock = Clock.fixed(Instant.parse("2026-03-02T19:00:00Z"), ZoneOffset.UTC)
    private val bali = TestData.destination("bali")
    private val kyoto = TestData.destination("kyoto")

    private fun vm(
        destinations: FakeDestinationRepository = FakeDestinationRepository(listOf(bali, kyoto)),
        trips: FakeTripRepository = FakeTripRepository(),
        saved: FakeSavedDestinationRepository = FakeSavedDestinationRepository(listOf(bali, kyoto)),
    ) = HomeViewModel(destinations, trips, saved, ObserveSaveableDestinationsUseCase(saved), clock)

    @Test fun `sections start loading`() = runTest {
        val state = vm(destinations = FakeDestinationRepository(null), trips = FakeTripRepository(null)).uiState.value
        assertEquals(LoadState.Loading, state.popular)
        assertEquals(LoadState.Loading, state.upcoming)
        assertEquals(Greeting.EVENING, state.greeting)
    }

    @Test fun `upcoming skips past trips and empty means Ready(null)`() = runTest {
        val past = TestData.trip("past", today.minusDays(10))
        val next = TestData.trip("next", today.plusDays(5))
        val trips = FakeTripRepository(listOf(past, next))
        val vm = vm(trips = trips)
        collectInBackground(vm.uiState)
        assertEquals("next", (vm.uiState.value.upcoming as LoadState.Ready).data?.trip?.id)

        trips.trips.value = listOf(past)
        assertEquals(LoadState.Ready(null), vm.uiState.first { (it.upcoming as? LoadState.Ready)?.data == null }.upcoming)
    }

    @Test fun `toggling save updates popular and recently saved`() = runTest {
        val vm = vm()
        collectInBackground(vm.uiState)
        vm.onAction(HomeAction.ToggleSave("kyoto", true))
        val state = vm.uiState.value
        assertTrue((state.popular as LoadState.Ready).data.first { it.destination.id == "kyoto" }.isSaved)
        assertEquals(listOf("kyoto"), (state.recentlySaved as LoadState.Ready).data.map { it.destination.id })
    }

    @Test fun `greeting by hour`() {
        assertEquals(Greeting.MORNING, HomeViewModel.greetingFor(LocalTime.of(8, 0)))
        assertEquals(Greeting.AFTERNOON, HomeViewModel.greetingFor(LocalTime.of(13, 0)))
        assertEquals(Greeting.EVENING, HomeViewModel.greetingFor(LocalTime.of(22, 0)))
    }
}
