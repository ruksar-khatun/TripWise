package com.tripwise.core.common.result

import com.tripwise.core.common.error.AppError
import kotlin.coroutines.cancellation.CancellationException

/**
 * Result type used across repository boundaries. Repositories never throw to callers;
 * they return [Outcome.Failure] with a classified [AppError].
 */
sealed interface Outcome<out T> {
    data class Success<out T>(val value: T) : Outcome<T>
    data class Failure(val error: AppError) : Outcome<Nothing>
}

inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(value))
    is Outcome.Failure -> this
}

inline fun <T> Outcome<T>.onSuccess(action: (T) -> Unit): Outcome<T> {
    if (this is Outcome.Success) action(value)
    return this
}

inline fun <T> Outcome<T>.onFailure(action: (AppError) -> Unit): Outcome<T> {
    if (this is Outcome.Failure) action(error)
    return this
}

fun <T> Outcome<T>.getOrNull(): T? = (this as? Outcome.Success)?.value

/**
 * Runs [block], converting thrown exceptions with [mapError] while always re-throwing
 * [CancellationException] so structured concurrency keeps working.
 */
inline fun <T> outcomeOf(mapError: (Throwable) -> AppError, block: () -> T): Outcome<T> =
    try {
        Outcome.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Outcome.Failure(mapError(e))
    }
