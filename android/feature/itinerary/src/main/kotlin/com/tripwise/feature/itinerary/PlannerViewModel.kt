package com.tripwise.feature.itinerary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.domain.repository.ItineraryPlanRepository
import com.tripwise.core.domain.repository.TripRepository
import com.tripwise.core.model.BudgetLevel
import com.tripwise.core.model.Destination
import com.tripwise.core.model.ItineraryItem
import com.tripwise.core.model.ItineraryPlan
import com.tripwise.core.model.ItineraryRequest
import com.tripwise.core.model.NewTrip
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.model.TravelPace
import com.tripwise.core.model.Trip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class PlannerUiState(
    val destinations: List<Destination> = emptyList(),
    val destinationId: String? = null,
    val days: Int = 3,
    val travellers: Int = 2,
    val pace: TravelPace = TravelPace.BALANCED,
    val budgetLevel: BudgetLevel = BudgetLevel.MID,
    val interests: Set<TravelInterest> = emptySet(),
    val notes: String = "",
    val startDate: LocalDate,
    /** Set when planning for an existing trip (dates and destination come from it). */
    val trip: Trip? = null,
    /** Null before the first generation. */
    val plan: LoadState<ItineraryPlan>? = null,
    val saving: Boolean = false,
    val saveError: AppError? = null,
    val savedTripId: String? = null,
) {
    val destination: Destination? get() = destinations.firstOrNull { it.id == destinationId }
    val canGenerate: Boolean get() = destinationId != null && plan !is LoadState.Loading
}

sealed interface PlannerAction {
    data class SelectDestination(val id: String) : PlannerAction
    data class SetDays(val days: Int) : PlannerAction
    data class SetTravellers(val travellers: Int) : PlannerAction
    data class SetPace(val pace: TravelPace) : PlannerAction
    data class SetBudget(val level: BudgetLevel) : PlannerAction
    data class ToggleInterest(val interest: TravelInterest) : PlannerAction
    data class SetNotes(val notes: String) : PlannerAction
    data class SetStartDate(val date: LocalDate) : PlannerAction
    data object Generate : PlannerAction
    data object EditPreferences : PlannerAction
    data object Save : PlannerAction
}

@HiltViewModel
class PlannerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val destinationRepository: DestinationRepository,
    private val planner: ItineraryPlanRepository,
    private val trips: TripRepository,
    clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlannerUiState(startDate = LocalDate.now(clock).plusDays(30)))
    val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()

    init {
        val tripId: String? = savedStateHandle["tripId"]
        val destinationId: String? = savedStateHandle["destinationId"]
        viewModelScope.launch {
            val trip = tripId?.let { trips.observeTrip(it).firstOrNull() }
            val all = runCatching { destinationRepository.observeDestinations().first() }.getOrDefault(emptyList())
            _uiState.update {
                it.copy(
                    destinations = all,
                    trip = trip,
                    destinationId = destinationId ?: trip?.destinationId ?: it.destinationId,
                    days = trip?.dayCount?.coerceIn(MIN_DAYS, MAX_DAYS) ?: it.days,
                    travellers = trip?.travellers ?: it.travellers,
                    startDate = trip?.startDate ?: it.startDate,
                )
            }
        }
    }

    fun onAction(action: PlannerAction) {
        when (action) {
            is PlannerAction.SelectDestination -> _uiState.update { it.copy(destinationId = action.id) }
            is PlannerAction.SetDays -> _uiState.update { if (it.trip != null) it else it.copy(days = action.days.coerceIn(MIN_DAYS, MAX_DAYS)) }
            is PlannerAction.SetTravellers -> _uiState.update { it.copy(travellers = action.travellers.coerceIn(1, 20)) }
            is PlannerAction.SetPace -> _uiState.update { it.copy(pace = action.pace) }
            is PlannerAction.SetBudget -> _uiState.update { it.copy(budgetLevel = action.level) }
            is PlannerAction.ToggleInterest -> _uiState.update {
                it.copy(interests = if (action.interest in it.interests) it.interests - action.interest else it.interests + action.interest)
            }
            is PlannerAction.SetNotes -> _uiState.update { it.copy(notes = action.notes.take(300)) }
            is PlannerAction.SetStartDate -> _uiState.update { it.copy(startDate = action.date) }
            PlannerAction.Generate -> generate()
            PlannerAction.EditPreferences -> _uiState.update { it.copy(plan = null, saveError = null) }
            PlannerAction.Save -> save()
        }
    }

    private fun generate() {
        val s = _uiState.value
        val destinationId = s.destinationId ?: return
        if (!s.canGenerate) return
        _uiState.update { it.copy(plan = LoadState.Loading, saveError = null) }
        viewModelScope.launch {
            val request = ItineraryRequest(destinationId, s.days, s.travellers, s.pace, s.budgetLevel, s.interests, s.notes.ifBlank { null })
            val result = planner.generate(request)
            _uiState.update {
                it.copy(plan = when (result) {
                    is Outcome.Success -> LoadState.Ready(result.value)
                    is Outcome.Failure -> LoadState.Failed(result.error)
                })
            }
        }
    }

    private fun save() {
        val s = _uiState.value
        val plan = (s.plan as? LoadState.Ready)?.data ?: return
        if (s.saving || s.savedTripId != null) return
        _uiState.update { it.copy(saving = true, saveError = null) }
        viewModelScope.launch {
            val tripId = s.trip?.id ?: when (
                val created = trips.createTrip(
                    NewTrip(
                        title = plan.title,
                        destinationName = s.destination?.name ?: plan.title,
                        destinationId = plan.destinationId,
                        startDate = s.startDate,
                        endDate = s.startDate.plusDays(plan.days.size - 1L),
                        travellers = s.travellers,
                        budget = null,
                        notes = plan.summary,
                    ),
                )
            ) {
                is Outcome.Success -> created.value
                is Outcome.Failure -> {
                    _uiState.update { it.copy(saving = false, saveError = created.error) }
                    return@launch
                }
            }
            val start = s.trip?.startDate ?: s.startDate
            trips.addItineraryItems(tripId, plan.toItems(tripId, start))
            _uiState.update { it.copy(saving = false, savedTripId = tripId) }
        }
    }

    internal companion object {
        const val MIN_DAYS = 1
        const val MAX_DAYS = 14

        fun ItineraryPlan.toItems(tripId: String, start: LocalDate): List<ItineraryItem> = days.flatMap { day ->
            day.items.map {
                ItineraryItem(
                    id = UUID.randomUUID().toString(),
                    tripId = tripId,
                    date = start.plusDays(day.day - 1L),
                    startTime = it.startTime,
                    title = it.title,
                    category = it.category,
                    estimatedCost = it.estimatedCost,
                    isDone = false,
                    notes = it.notes,
                )
            }
        }
    }
}
