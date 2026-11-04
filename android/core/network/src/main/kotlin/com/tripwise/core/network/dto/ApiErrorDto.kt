package com.tripwise.core.network.dto

import kotlinx.serialization.Serializable

/** Error envelope every backend error response uses: `{ "error": { code, message, details? } }`. */
@Serializable
data class ApiErrorEnvelope(val error: ApiErrorDto)

@Serializable
data class ApiErrorDto(
    val code: String,
    val message: String,
    /** Field-level validation messages, if any. */
    val details: Map<String, String>? = null,
    /** Present on CONFLICT responses. */
    val serverVersion: Long? = null,
)

/** Backend error codes. Keep in sync with backend/src/http/errors.ts. */
object ApiErrorCode {
    const val VALIDATION_FAILED = "VALIDATION_FAILED"
    const val UNAUTHORIZED = "UNAUTHORIZED"
    const val FORBIDDEN = "FORBIDDEN"
    const val NOT_FOUND = "NOT_FOUND"
    const val CONFLICT = "CONFLICT"
    const val RATE_LIMITED = "RATE_LIMITED"
    const val AI_UNAVAILABLE = "AI_UNAVAILABLE"
    const val AI_QUOTA_EXCEEDED = "AI_QUOTA_EXCEEDED"
    const val AI_INVALID_OUTPUT = "AI_INVALID_OUTPUT"
    const val INTERNAL = "INTERNAL"
}
