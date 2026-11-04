package com.tripwise.feature.home

import com.tripwise.core.common.result.LoadState
import com.tripwise.core.model.BudgetSummary
import com.tripwise.core.model.SaveableDestination
import com.tripwise.core.model.Trip
import java.time.LocalDate

enum class Greeting { MORNING, AFTERNOON, EVENING }

data class UpcomingTrip(val trip: Trip, val budget: BudgetSummary?)

/**
 * Each dashboard section loads independently, so one failure never blanks the screen.
 * `LoadState.Ready(null)` for [upcoming] means "no upcoming trip" (empty state).
 */
data class HomeUiState(
    val today: LocalDate,
    val greeting: Greeting,
    val upcoming: LoadState<UpcomingTrip?> = LoadState.Loading,
    val popular: LoadState<List<SaveableDestination>> = LoadState.Loading,
    val recentlySaved: LoadState<List<SaveableDestination>> = LoadState.Loading,
)

sealed interface HomeAction {
    data class ToggleSave(val destinationId: String, val save: Boolean) : HomeAction
    data object Retry : HomeAction
}
