package com.tripwise.feature.destination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.common.result.asLoadState
import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.domain.repository.SavedDestinationRepository
import com.tripwise.core.model.Destination
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
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

sealed interface DestinationDetailUiState {
    data object Loading : DestinationDetailUiState
    data object NotFound : DestinationDetailUiState
    data class Failed(val error: com.tripwise.core.common.error.AppError) : DestinationDetailUiState
    data class Ready(val destination: Destination, val isSaved: Boolean) : DestinationDetailUiState
}

sealed interface DestinationDetailAction {
    data object ToggleSave : DestinationDetailAction
    data object Retry : DestinationDetailAction
}

/**
 * Assisted injection (id passed at creation) so the same ViewModel serves both the full-screen
 * route and the two-pane detail on tablets, each keyed by destination id.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = DestinationDetailViewModel.Factory::class)
class DestinationDetailViewModel @AssistedInject constructor(
    @Assisted private val destinationId: String,
    destinationRepository: DestinationRepository,
    private val savedRepository: SavedDestinationRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(destinationId: String): DestinationDetailViewModel
    }

    private val refresh = MutableStateFlow(0)

    val uiState: StateFlow<DestinationDetailUiState> = refresh.flatMapLatest {
        combine(destinationRepository.observeDestination(destinationId), savedRepository.observeSavedIds()) { d, saved ->
            d?.let { it to (it.id in saved) }
        }.asLoadState()
    }.map { load ->
        when (load) {
            LoadState.Loading -> DestinationDetailUiState.Loading
            is LoadState.Failed -> DestinationDetailUiState.Failed(load.error)
            is LoadState.Ready -> load.data?.let { (d, saved) -> DestinationDetailUiState.Ready(d, saved) }
                ?: DestinationDetailUiState.NotFound
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DestinationDetailUiState.Loading)

    fun onAction(action: DestinationDetailAction) {
        when (action) {
            DestinationDetailAction.ToggleSave -> {
                val ready = uiState.value as? DestinationDetailUiState.Ready ?: return
                viewModelScope.launch { savedRepository.setSaved(destinationId, !ready.isSaved) }
            }
            DestinationDetailAction.Retry -> refresh.value++
        }
    }
}
