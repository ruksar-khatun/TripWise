package com.tripwise.core.common.error

/**
 * Every failure the app can surface, independent of where it came from (network, database, AI).
 * The data layer maps raw exceptions/HTTP errors into these; the UI maps these into messages.
 */
sealed interface AppError {
    /** No connectivity or an I/O failure before a response arrived. Retryable. */
    data object Network : AppError

    /** The request started but took too long. Retryable. */
    data object Timeout : AppError

    /** Missing/expired credentials. The session layer will route to sign-in once auth exists. */
    data object Unauthorized : AppError

    /** Sign-in or password confirmation failed (wrong email or password). */
    data object InvalidCredentials : AppError

    /** Authenticated but not allowed to access the resource. */
    data object Forbidden : AppError

    data object NotFound : AppError

    /** Input rejected by the server; [fields] maps field name to a server message. */
    data class Validation(val fields: Map<String, String> = emptyMap()) : AppError

    data object RateLimited : AppError

    /** Optimistic-concurrency conflict during sync. */
    data class Conflict(val serverVersion: Long? = null) : AppError

    /** AI-specific failures reported by the backend. */
    data class Ai(val reason: AiFailure) : AppError

    /** A server-side integration (Places, Gemini) has no API key configured. Not retryable by the user. */
    data object NotConfigured : AppError

    /** Server-side failure (5xx). Retryable. */
    data class Server(val httpCode: Int) : AppError

    /** Anything not classified above, including unparseable responses. */
    data class Unknown(val cause: Throwable? = null) : AppError
}

enum class AiFailure { UNAVAILABLE, QUOTA_EXCEEDED, INVALID_OUTPUT }

/** Whether offering "Try again" makes sense for this error. */
val AppError.isRetryable: Boolean
    get() = when (this) {
        AppError.Network, AppError.Timeout, AppError.RateLimited, is AppError.Server -> true
        is AppError.Ai -> reason != AiFailure.QUOTA_EXCEEDED
        is AppError.Unknown -> true
        else -> false
    }
