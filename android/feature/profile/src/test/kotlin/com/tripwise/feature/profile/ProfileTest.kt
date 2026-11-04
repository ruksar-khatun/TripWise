package com.tripwise.feature.profile

import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.domain.usecase.ObserveTravelStatsUseCase
import com.tripwise.core.model.SessionState
import com.tripwise.core.model.TravelStats
import com.tripwise.core.testing.data.TestData
import com.tripwise.core.testing.fakes.FakeAuthRepository
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

    private val bali = TestData.destination("bali", country = "Indonesia")
    private val ubud = TestData.destination("ubud", country = "Indonesia")
    private val kyoto = TestData.destination("kyoto", country = "Japan")
    private val day = LocalDate.of(2026, 1, 1)
    private val auth = FakeAuthRepository(SessionState.SignedIn(TestData.profile))

    private fun vm() = ProfileViewModel(
        FakeUserRepository(TestData.profile),
        ObserveTravelStatsUseCase(
            FakeTripRepository(listOf(TestData.trip("1", day, destinationId = "bali"), TestData.trip("2", day, destinationId = "ubud"), TestData.trip("3", day, destinationId = "kyoto"))),
            FakeSavedDestinationRepository(listOf(bali), mapOf("bali" to Instant.EPOCH)),
            FakeDestinationRepository(listOf(bali, ubud, kyoto)),
        ),
        auth,
    )

    @Test fun `initials`() {
        assertEquals("AT", initials("Alex Traveller"))
        assertEquals("M", initials("Madonna"))
        assertEquals("?", initials("  "))
    }

    @Test fun `stats count trips, saved and distinct countries`() = runTest {
        val vm = vm()
        collectInBackground(vm.uiState)
        assertEquals(TravelStats(trips = 3, saved = 1, countries = 2), (vm.uiState.value.content as LoadState.Ready).data.stats)
    }

    @Test fun `sign out goes through the auth repository`() = runTest {
        val vm = vm()
        collectInBackground(vm.uiState)
        vm.onAction(ProfileAction.OpenDialog(ProfileDialog.SIGN_OUT))
        vm.onAction(ProfileAction.ConfirmSignOut)
        assertEquals(listOf("signOut"), auth.calls)
        assertEquals(SessionState.SignedOut(), auth.session.value)
    }

    @Test fun `wrong password keeps the delete dialog open with an error`() = runTest {
        val vm = vm()
        collectInBackground(vm.uiState)
        auth.nextResult = Outcome.Failure(AppError.InvalidCredentials)
        vm.onAction(ProfileAction.OpenDialog(ProfileDialog.DELETE_ACCOUNT))
        vm.onAction(ProfileAction.ConfirmDelete("nope"))
        assertEquals(ProfileDialog.DELETE_ACCOUNT, vm.uiState.value.dialog)
        assertEquals(AppError.InvalidCredentials, vm.uiState.value.deleteError)
        assert(auth.session.value is SessionState.SignedIn)
    }
}
