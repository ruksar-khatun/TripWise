package com.tripwise.feature.home

import app.cash.turbine.test
import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.model.BackendStatus
import com.tripwise.core.testing.fakes.FakeSystemRepository
import com.tripwise.core.testing.rules.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val status = BackendStatus("tripwise-backend", "0.1.0", true)

    @Test fun `shows checking then connected`() = runTest {
        val repo = FakeSystemRepository(Outcome.Success(status)).apply { gate = CompletableDeferred() }
        val vm = HomeViewModel(repo)
        vm.uiState.test {
            assertEquals(BackendCheck.Checking, awaitItem().backend)
            repo.gate!!.complete(Unit)
            assertEquals(BackendCheck.Connected(status), awaitItem().backend)
        }
    }

    @Test fun `failure is exposed as AppError and retry re-checks`() = runTest {
        val repo = FakeSystemRepository(Outcome.Failure(AppError.Network))
        val vm = HomeViewModel(repo)
        assertEquals(BackendCheck.Failed(AppError.Network), vm.uiState.value.backend)

        repo.nextResult = Outcome.Success(status)
        vm.onAction(HomeAction.RetryBackendCheck)
        assertEquals(BackendCheck.Connected(status), vm.uiState.value.backend)
        assertEquals(2, repo.callCount)
    }
}
