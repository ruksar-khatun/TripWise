package com.tripwise.feature.home

import com.tripwise.core.common.error.AppError
import com.tripwise.core.model.BackendStatus

/**
 * Screen state: one immutable object, exposed as StateFlow by [HomeViewModel].
 * Dashboard sections (upcoming trip, saved, suggestions) become fields here in the Home phase.
 */
data class HomeUiState(
    val backend: BackendCheck = BackendCheck.Checking,
)

sealed interface BackendCheck {
    data object Checking : BackendCheck
    data class Connected(val status: BackendStatus) : BackendCheck
    data class Failed(val error: AppError) : BackendCheck
}

/** All user intents for the screen go through a single `onAction` entry point. */
sealed interface HomeAction {
    data object RetryBackendCheck : HomeAction
}
