package com.tripwise.core.testing.fakes

import com.tripwise.core.common.result.Outcome
import com.tripwise.core.domain.repository.SystemRepository
import com.tripwise.core.model.BackendStatus
import kotlinx.coroutines.CompletableDeferred

/** Configurable fake. Set [nextResult], or use [gate] to hold the call open to observe loading states. */
class FakeSystemRepository(
    var nextResult: Outcome<BackendStatus> = Outcome.Success(BackendStatus("tripwise-backend", "test", true)),
) : SystemRepository {
    var gate: CompletableDeferred<Unit>? = null
    var callCount = 0
        private set

    override suspend fun checkBackend(): Outcome<BackendStatus> {
        callCount++
        gate?.await()
        return nextResult
    }
}
