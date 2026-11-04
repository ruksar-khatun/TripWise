package com.tripwise.feature.profile

import com.tripwise.core.common.result.LoadState
import com.tripwise.core.domain.usecase.ObserveTravelStatsUseCase
import com.tripwise.core.model.TravelStats
import com.tripwise.core.testing.data.TestData
import com.tripwise.core.testing.fakes.FakeDestinationRepository
import com.tripwise.core.testing.fakes.FakeSavedDestinationRepository
import com.tripwise.core.testing.fakes.FakeTripRepository
import com.tripwise.core.testing.fakes.FakeUserRepository
import com.tripwise.core.testing.rules.MainDispatcherRule
import com.tripwise.core.testing.rules.collectInBackground
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class ProfileTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test fun `initials`() {
        assertEquals("AT", initials("Alex Traveller"))
        assertEquals("M", initials("Madonna"))
        assertEquals("?", initials("  "))
    }

    @Test fun `stats count trips, saved and distinct countries`() = runTest {
        val bali = TestData.destination("bali", country = "Indonesia")
        val ubud = TestData.destination("ubud", country = "Indonesia")
        val kyoto = TestData.destination("kyoto", country = "Japan")
        val day = LocalDate.of(2026, 1, 1)
        val trips = FakeTripRepository(
            listOf(TestData.trip("1", day, destinationId = "bali"), TestData.trip("2", day, destinationId = "ubud"), TestData.trip("3", day, destinationId = "kyoto")),
        )
        val saved = FakeSavedDestinationRepository(listOf(bali), mapOf("bali" to Instant.EPOCH))
        val vm = ProfileViewModel(
            FakeUserRepository(TestData.profile),
            ObserveTravelStatsUseCase(trips, saved, FakeDestinationRepository(listOf(bali, ubud, kyoto))),
        )
        collectInBackground(vm.uiState)
        assertEquals(TravelStats(trips = 3, saved = 1, countries = 2), (vm.uiState.value as LoadState.Ready).data.stats)
    }
}
