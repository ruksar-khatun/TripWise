package com.tripwise.core.ui.error

import androidx.annotation.StringRes
import com.tripwise.core.common.error.AiFailure
import com.tripwise.core.common.error.AppError
import com.tripwise.core.ui.R

/** The single mapping from [AppError] to user-facing copy. Features may override for context. */
@StringRes
fun AppError.messageRes(): Int = when (this) {
    AppError.Network -> R.string.core_ui_error_network
    AppError.Timeout -> R.string.core_ui_error_timeout
    AppError.Unauthorized -> R.string.core_ui_error_unauthorized
    AppError.InvalidCredentials -> R.string.core_ui_error_invalid_credentials
    AppError.Forbidden -> R.string.core_ui_error_forbidden
    AppError.NotFound -> R.string.core_ui_error_not_found
    is AppError.Validation -> R.string.core_ui_error_validation
    AppError.RateLimited -> R.string.core_ui_error_rate_limited
    is AppError.Conflict -> R.string.core_ui_error_conflict
    is AppError.Ai -> when (reason) {
        AiFailure.UNAVAILABLE -> R.string.core_ui_error_ai_unavailable
        AiFailure.QUOTA_EXCEEDED -> R.string.core_ui_error_ai_quota
        AiFailure.INVALID_OUTPUT -> R.string.core_ui_error_ai_invalid
    }
    AppError.NotConfigured -> R.string.core_ui_error_not_configured
    is AppError.Server -> R.string.core_ui_error_server
    is AppError.Unknown -> R.string.core_ui_error_unknown
}
