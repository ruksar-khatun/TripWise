package com.tripwise.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwise.core.common.result.asLoadState
import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.domain.repository.SavedDestinationRepository
import com.tripwise.core.domain.repository.TripRepository
import com.tripwise.core.domain.usecase.ObserveSaveableDestinationsUseCase
import com.tripwise.core.model.SaveableDestination
import com.tripwise.core.model.TripStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    destinationRepository: DestinationRepository,
    tripRepository: TripRepository,
    private val savedRepository: SavedDestinationRepository,
    observeSaveable: ObserveSaveableDestinationsUseCase,
    clock: Clock,
) : ViewModel() {

    private val today = LocalDate.now(clock)
    private val greeting = greetingFor(LocalTime.now(clock))

    /** Bumped by Retry; every section restarts its upstream flow. */
    private val refresh = MutableStateFlow(0)

    private val upcoming = refresh.flatMapLatest {
        tripRepository.observeTrips()
            .map { trips -> trips.firstOrNull { it.statusOn(today) != TripStatus.PAST } }
            .flatMapLatest { trip ->
                if (trip == null) flowOf(null) else tripRepository.observeBudget(trip.id).map { UpcomingTrip(trip, it) }
            }
            .asLoadState()
    }

    private val popular = refresh.flatMapLatest {
        observeSaveable(destinationRepository.observePopular(limit = 8)).asLoadState()
    }

    private val featured = refresh.flatMapLatest {
        observeSaveable(destinationRepository.observeFeatured()).asLoadState()
    }

    private val recentlySaved = refresh.flatMapLatest {
        savedRepository.observeSaved()
            .map { list -> list.take(6).map { SaveableDestination(it.destination, isSaved = true) } }
            .asLoadState()
    }

    val uiState: StateFlow<HomeUiState> =
        combine(upcoming, featured, popular, recentlySaved) { upcoming, featured, popular, saved ->
            HomeUiState(today, greeting, upcoming, featured, popular, saved)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(today, greeting))

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.ToggleSave -> viewModelScope.launch { savedRepository.setSaved(action.destinationId, action.save) }
            HomeAction.Retry -> refresh.value++
        }
    }

    internal companion object {
        fun greetingFor(time: LocalTime): Greeting = when (time.hour) {
            in 5..11 -> Greeting.MORNING
            in 12..16 -> Greeting.AFTERNOON
            else -> Greeting.EVENING
        }
    }
}
