package com.tripwise.feature.destination.extras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.common.result.asLoadState
import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.domain.repository.PlacesRepository
import com.tripwise.core.model.ActivityCategory
import com.tripwise.core.model.Destination
import com.tripwise.core.model.NearbyPlace
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DestinationExtrasUiState(
    val destination: LoadState<Destination?> = LoadState.Loading,
    val category: ActivityCategory? = null,
    /** Null until requested: Places calls cost quota, so they only run when the user opens them. */
    val nearby: LoadState<List<NearbyPlace>>? = null,
) {
    val categories: List<ActivityCategory>
        get() = (destination as? LoadState.Ready)?.data?.attractions?.map { it.category }?.distinct().orEmpty()
}

sealed interface DestinationExtrasAction {
    data class SelectCategory(val category: ActivityCategory?) : DestinationExtrasAction
    data object LoadNearby : DestinationExtrasAction
    data object Retry : DestinationExtrasAction
}

/** Shared by the Activities and Map screens of one destination. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = DestinationExtrasViewModel.Factory::class)
class DestinationExtrasViewModel @AssistedInject constructor(
    @Assisted private val destinationId: String,
    destinations: DestinationRepository,
    private val places: PlacesRepository,
) : ViewModel() {
    @AssistedFactory interface Factory { fun create(destinationId: String): DestinationExtrasViewModel }

    private val refresh = MutableStateFlow(0)
    private val category = MutableStateFlow<ActivityCategory?>(null)
    private val nearby = MutableStateFlow<LoadState<List<NearbyPlace>>?>(null)

    val uiState: StateFlow<DestinationExtrasUiState> = combine(
        refresh.flatMapLatest { destinations.observeDestination(destinationId).asLoadState() },
        category,
        nearby,
    ) { d, c, n -> DestinationExtrasUiState(d, c, n) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DestinationExtrasUiState())

    fun onAction(action: DestinationExtrasAction) {
        when (action) {
            is DestinationExtrasAction.SelectCategory -> category.value = action.category
            DestinationExtrasAction.LoadNearby -> loadNearby()
            DestinationExtrasAction.Retry -> { refresh.value++; if (nearby.value is LoadState.Failed) loadNearby(force = true) }
        }
    }

    private fun loadNearby(force: Boolean = false) {
        val current = nearby.value
        if (!force && (current is LoadState.Loading || current is LoadState.Ready)) return
        val destination = (uiState.value.destination as? LoadState.Ready)?.data ?: return
        nearby.value = LoadState.Loading
        viewModelScope.launch {
            nearby.value = when (val r = places.nearby(destination.location)) {
                is Outcome.Success -> LoadState.Ready(r.value)
                is Outcome.Failure -> LoadState.Failed(r.error)
            }
        }
    }

    internal fun nearbyError(): AppError? = (nearby.value as? LoadState.Failed)?.error
}
