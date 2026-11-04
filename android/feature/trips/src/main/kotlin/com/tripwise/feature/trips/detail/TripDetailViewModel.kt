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

/** Add/edit itinerary item dialog. [id] null = new. */
data class ItemEditor(
    val id: String?,
    val date: java.time.LocalDate,
    val time: String,
    val title: String,
    val category: com.tripwise.core.model.ActivityCategory,
    val notes: String,
    val isDone: Boolean = false,
    val showErrors: Boolean = false,
) {
    val parsedTime: java.time.LocalTime? get() = runCatching { java.time.LocalTime.parse(time.trim().padStart(5, '0')) }.getOrNull()
    val valid: Boolean get() = title.isNotBlank() && (time.isBlank() || parsedTime != null)
}

sealed interface TripDetailAction {
    data class AddItem(val date: java.time.LocalDate) : TripDetailAction
    data class EditItem(val item: ItineraryItem) : TripDetailAction
    data class UpdateEditor(val editor: ItemEditor) : TripDetailAction
    data object SaveItem : TripDetailAction
    data object DeleteItem : TripDetailAction
    data object DismissEditor : TripDetailAction
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
    private val _editor = MutableStateFlow<ItemEditor?>(null)
    val editor: StateFlow<ItemEditor?> = _editor

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

    private fun saveItem() {
        val e = _editor.value ?: return
        if (!e.valid) { _editor.value = e.copy(showErrors = true); return }
        val existing = (uiState.value as? TripDetailUiState.Ready)?.days?.flatMap { it.items }?.firstOrNull { it.id == e.id }
        val item = ItineraryItem(
            id = e.id ?: java.util.UUID.randomUUID().toString(), tripId = tripId, date = e.date, startTime = e.parsedTime,
            title = e.title.trim(), category = e.category, estimatedCost = existing?.estimatedCost, isDone = e.isDone,
            notes = e.notes.trim().ifEmpty { null },
        )
        viewModelScope.launch { tripRepository.saveItineraryItem(item) }
        _editor.value = null
    }

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
            is TripDetailAction.AddItem -> _editor.value = ItemEditor(null, action.date, "", "", com.tripwise.core.model.ActivityCategory.SIGHTSEEING, "")
            is TripDetailAction.EditItem -> _editor.value = with(action.item) {
                ItemEditor(id, date, startTime?.toString().orEmpty(), title, category, notes.orEmpty(), isDone)
            }
            is TripDetailAction.UpdateEditor -> _editor.value = action.editor
            TripDetailAction.DismissEditor -> _editor.value = null
            TripDetailAction.DeleteItem -> _editor.value?.id?.let { id ->
                viewModelScope.launch { tripRepository.deleteItineraryItem(id) }
                _editor.value = null
            }
            TripDetailAction.SaveItem -> saveItem()
        }
    }
}
