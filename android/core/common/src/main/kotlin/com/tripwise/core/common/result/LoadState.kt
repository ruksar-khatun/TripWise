package com.tripwise.core.common.result

import com.tripwise.core.common.error.AppError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/** State of an observed data stream as seen by a screen section. */
sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Ready<out T>(val data: T) : LoadState<T>
    data class Failed(val error: AppError) : LoadState<Nothing>
}

/** Thrown inside flows to carry an already-classified error. */
class AppErrorException(val error: AppError) : Exception(error.toString())

/**
 * Wraps a repository Flow for the UI: starts with Loading, maps values to Ready, and turns
 * upstream exceptions into Failed instead of crashing the collector.
 */
fun <T> Flow<T>.asLoadState(): Flow<LoadState<T>> = map<T, LoadState<T>> { LoadState.Ready(it) }
    .onStart { emit(LoadState.Loading) }
    .catch { e -> emit(LoadState.Failed((e as? AppErrorException)?.error ?: AppError.Unknown(e))) }

fun <T> LoadState<T>.dataOrNull(): T? = (this as? LoadState.Ready)?.data
