package com.tripwise.core.network.error

import com.tripwise.core.common.error.AiFailure
import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.common.result.outcomeOf
import com.tripwise.core.network.dto.ApiErrorCode
import com.tripwise.core.network.dto.ApiErrorDto
import com.tripwise.core.network.dto.ApiErrorEnvelope
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.io.InterruptedIOException
import javax.inject.Inject
import javax.inject.Singleton

/** Converts anything thrown by a Retrofit call into a classified [AppError]. */
@Singleton
class ApiErrorMapper @Inject constructor(private val json: Json) {

    fun map(throwable: Throwable): AppError = when (throwable) {
        is HttpException -> fromHttp(throwable)
        // SocketTimeoutException extends InterruptedIOException; check before generic IOException.
        is InterruptedIOException -> AppError.Timeout
        is IOException -> AppError.Network
        is SerializationException, is IllegalArgumentException -> AppError.Unknown(throwable)
        else -> AppError.Unknown(throwable)
    }

    private fun fromHttp(e: HttpException): AppError {
        val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        val dto = body?.let { runCatching { json.decodeFromString<ApiErrorEnvelope>(it).error }.getOrNull() }
        return dto?.let(::fromCode) ?: fromStatus(e.code())
    }

    private fun fromCode(dto: ApiErrorDto): AppError? = when (dto.code) {
        ApiErrorCode.VALIDATION_FAILED -> AppError.Validation(dto.details.orEmpty())
        ApiErrorCode.UNAUTHORIZED -> AppError.Unauthorized
        ApiErrorCode.FORBIDDEN -> AppError.Forbidden
        ApiErrorCode.NOT_FOUND -> AppError.NotFound
        ApiErrorCode.CONFLICT -> AppError.Conflict(dto.serverVersion)
        ApiErrorCode.RATE_LIMITED -> AppError.RateLimited
        ApiErrorCode.AI_UNAVAILABLE -> AppError.Ai(AiFailure.UNAVAILABLE)
        ApiErrorCode.AI_QUOTA_EXCEEDED -> AppError.Ai(AiFailure.QUOTA_EXCEEDED)
        ApiErrorCode.AI_INVALID_OUTPUT -> AppError.Ai(AiFailure.INVALID_OUTPUT)
        else -> null // unknown code: fall back to HTTP status
    }

    private fun fromStatus(code: Int): AppError = when (code) {
        400, 422 -> AppError.Validation()
        401 -> AppError.Unauthorized
        403 -> AppError.Forbidden
        404 -> AppError.NotFound
        409 -> AppError.Conflict()
        429 -> AppError.RateLimited
        in 500..599 -> AppError.Server(code)
        else -> AppError.Unknown()
    }
}

/** Wraps a Retrofit call: success → [Outcome.Success], any failure → classified [Outcome.Failure]. */
suspend inline fun <T> ApiErrorMapper.call(crossinline block: suspend () -> T): Outcome<T> =
    outcomeOf({ map(it) }) { block() }
