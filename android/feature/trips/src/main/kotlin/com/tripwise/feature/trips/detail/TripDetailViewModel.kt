package com.tripwise.feature.trips.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.common.result.asLoadState
import com.tripwise.core.domain.repository.TripRepository
import com.tripwise.core.model.BudgetSummary
import com.tripwise.core.model.ItineraryItem
import com.tripwise.core.model.Trip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class ItineraryDay(val index: Int, val date: LocalDate, val items: List<ItineraryItem>)

sealed interface TripDetailUiState {
    data object Loading : TripDetailUiState
    data object NotFound : TripDetailUiState
    data class Failed(val error: AppError) : TripDetailUiState
    data class Ready(
        val trip: Trip,
        val today: LocalDate,
        /** One entry per trip day, including days with nothing planned. */
        val days: List<ItineraryDay>,
        val budget: BudgetSummary?,
        val nextItem: ItineraryItem?,
    ) : TripDetailUiState
}

sealed interface TripDetailAction {
    data class SetItemDone(val itemId: String, val done: Boolean) : TripDetailAction
    data object Retry : TripDetailAction
    data object Delete : TripDetailAction
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TripDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tripRepository: TripRepository,
    private val clock: Clock,
) : ViewModel() {

    /** Same key as the TripDetailRoute argument. */
    private val tripId: String = checkNotNull(savedStateHandle["tripId"]) { "tripId argument missing" }
    private val refresh = MutableStateFlow(0)

    val uiState: StateFlow<TripDetailUiState> = refresh.flatMapLatest {
        combine(
            tripRepository.observeTrip(tripId),
            tripRepository.observeItinerary(tripId),
            tripRepository.observeBudget(tripId),
        ) { trip, items, budget -> Triple(trip, items, budget) }.asLoadState()
    }.map { load ->
        when (load) {
            LoadState.Loading -> TripDetailUiState.Loading
            is LoadState.Failed -> TripDetailUiState.Failed(load.error)
            is LoadState.Ready -> {
                val (trip, items, budget) = load.data
                if (trip == null) TripDetailUiState.NotFound else ready(trip, items, budget)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TripDetailUiState.Loading)

    private fun ready(trip: Trip, items: List<ItineraryItem>, budget: BudgetSummary?): TripDetailUiState.Ready {
        val byDate = items.groupBy { it.date }
        val days = (0 until trip.dayCount).map { i ->
            val date = trip.startDate.plusDays(i.toLong())
            ItineraryDay(i + 1, date, byDate[date].orEmpty())
        }
        val now = LocalDateTime.now(clock)
        val next = items.filter { !it.isDone }
            .firstOrNull { it.date.atTime(it.startTime ?: java.time.LocalTime.MIDNIGHT) >= now }
            ?: items.firstOrNull { !it.isDone }
        return TripDetailUiState.Ready(trip, now.toLocalDate(), days, budget, next)
    }

    fun onAction(action: TripDetailAction) {
        when (action) {
            is TripDetailAction.SetItemDone -> viewModelScope.launch { tripRepository.setItineraryItemDone(action.itemId, action.done) }
            TripDetailAction.Retry -> refresh.value++
            TripDetailAction.Delete -> viewModelScope.launch { tripRepository.deleteTrip(tripId) }
        }
    }
}
