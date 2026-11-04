package com.tripwise.feature.assistant

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Assistant shell for the UI phase: resolves the conversation context (trip or destination) only. */
@HiltViewModel
class AssistantViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    tripRepository: TripRepository,
    destinationRepository: DestinationRepository,
) : ViewModel() {
    private val tripId: String? = savedStateHandle["tripId"]
    private val destinationId: String? = savedStateHandle["destinationId"]

    /** Name shown in the context pill, or null for a general conversation. */
    val contextTitle: StateFlow<String?> = when {
        tripId != null -> tripRepository.observeTrip(tripId).map { it?.title }
        destinationId != null -> destinationRepository.observeDestination(destinationId).map { it?.name }
        else -> flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
