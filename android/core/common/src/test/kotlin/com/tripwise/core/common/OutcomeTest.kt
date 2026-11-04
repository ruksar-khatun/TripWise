package com.tripwise.core.common

import com.tripwise.core.common.error.AiFailure
import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.error.isRetryable
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.common.result.getOrNull
import com.tripwise.core.common.result.map
import com.tripwise.core.common.result.outcomeOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class OutcomeTest {
    @Test fun `map transforms success and keeps failure`() {
        assertEquals(Outcome.Success(4), Outcome.Success(2).map { it * 2 })
        val failure: Outcome<Int> = Outcome.Failure(AppError.NotFound)
        assertEquals(failure, failure.map { it * 2 })
    }

    @Test fun `outcomeOf maps exceptions`() {
        val result = outcomeOf({ AppError.Unknown(it) }) { error("boom") }
        assertTrue((result as Outcome.Failure).error is AppError.Unknown)
        assertNull(result.getOrNull())
    }

    @Test(expected = CancellationException::class)
    fun `outcomeOf rethrows cancellation`() {
        outcomeOf({ AppError.Unknown(it) }) { throw CancellationException("cancelled") }
    }

    @Test fun `retryability`() {
        assertTrue(AppError.Network.isRetryable)
        assertTrue(AppError.Server(503).isRetryable)
        assertFalse(AppError.Unauthorized.isRetryable)
        assertFalse(AppError.Validation().isRetryable)
        assertFalse(AppError.Ai(AiFailure.QUOTA_EXCEEDED).isRetryable)
        assertTrue(AppError.Ai(AiFailure.UNAVAILABLE).isRetryable)
    }
}
