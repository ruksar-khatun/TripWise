package com.tripwise.core.common

import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.AppErrorException
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.common.result.asLoadState
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LoadStateTest {
    @Test fun `emits loading then ready`() = runTest {
        assertEquals(listOf(LoadState.Loading, LoadState.Ready(1)), flowOf(1).asLoadState().toList())
    }

    @Test fun `classified errors are preserved, others become Unknown`() = runTest {
        val classified = flow<Int> { throw AppErrorException(AppError.NotFound) }.asLoadState().toList()
        assertEquals(LoadState.Failed(AppError.NotFound), classified.last())
        val unknown = flow<Int> { error("x") }.asLoadState().toList().last()
        assert(unknown is LoadState.Failed && unknown.error is AppError.Unknown)
    }
}
