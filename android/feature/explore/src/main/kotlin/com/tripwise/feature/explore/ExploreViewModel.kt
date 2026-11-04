package com.tripwise.feature.explore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.common.result.asLoadState
import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.domain.repository.SavedDestinationRepository
import com.tripwise.core.domain.usecase.ObserveSaveableDestinationsUseCase
import com.tripwise.core.model.SaveableDestination
import com.tripwise.core.model.TravelInterest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExploreUiState(
    val query: String = "",
    val interest: TravelInterest? = null,
    val results: LoadState<List<SaveableDestination>> = LoadState.Loading,
)

sealed interface ExploreAction {
    data class QueryChanged(val query: String) : ExploreAction
    data class InterestSelected(val interest: TravelInterest?) : ExploreAction
    data object ClearFilters : ExploreAction
    data object Retry : ExploreAction
    data class ToggleSave(val destinationId: String, val save: Boolean) : ExploreAction
}

internal const val SEARCH_DEBOUNCE_MS = 300L

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    destinationRepository: DestinationRepository,
    private val savedRepository: SavedDestinationRepository,
    observeSaveable: ObserveSaveableDestinationsUseCase,
) : ViewModel() {

    // Survive process death. The "interest" key is also the ExploreRoute argument name.
    private val query = savedStateHandle.getStateFlow(KEY_QUERY, "")
    private val interest = savedStateHandle.getStateFlow<String?>(KEY_INTEREST, null)
        .map { name -> name?.let { runCatching { TravelInterest.valueOf(it) }.getOrNull() } }

    private val refresh = MutableStateFlow(0)

    private val results = combine(
        query.debounce { if (it.isBlank()) 0 else SEARCH_DEBOUNCE_MS }.map { it.trim() }.distinctUntilChanged(),
        interest,
        refresh,
    ) { q, i, _ -> q to i }
        .flatMapLatest { (q, i) -> observeSaveable(destinationRepository.observeDestinations(q, i)).asLoadState() }

    val uiState: StateFlow<ExploreUiState> = combine(query, interest, results) { q, i, r -> ExploreUiState(q, i, r) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExploreUiState())

    fun onAction(action: ExploreAction) {
        when (action) {
            is ExploreAction.QueryChanged -> savedStateHandle[KEY_QUERY] = action.query
            is ExploreAction.InterestSelected -> savedStateHandle[KEY_INTEREST] = action.interest?.name
            ExploreAction.ClearFilters -> {
                savedStateHandle[KEY_QUERY] = ""
                savedStateHandle[KEY_INTEREST] = null
            }
            ExploreAction.Retry -> refresh.value++
            is ExploreAction.ToggleSave -> viewModelScope.launch { savedRepository.setSaved(action.destinationId, action.save) }
        }
    }

    internal companion object {
        const val KEY_QUERY = "query"
        const val KEY_INTEREST = "interest"
    }
}
