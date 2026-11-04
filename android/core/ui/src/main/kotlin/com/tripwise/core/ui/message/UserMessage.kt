package com.tripwise.core.ui.message

import androidx.annotation.StringRes
import com.tripwise.core.common.error.AppError
import com.tripwise.core.ui.error.messageRes

/**
 * A transient message (snackbar) held in UI state. The screen shows it, then calls the
 * ViewModel's "message shown" action, which clears it. This avoids lost one-off events.
 */
data class UserMessage(
    @StringRes val textRes: Int,
    val id: Long = System.nanoTime(),
)

fun AppError.toUserMessage(): UserMessage = UserMessage(messageRes())
