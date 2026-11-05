package com.tripwise.feature.destination.extras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.common.result.asLoadState
import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.model.ActivityCategory
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
import kotlinx.coroutines.flow.stateIn

data class DestinationExtrasUiState(
    val destination: LoadState<Destination?> = LoadState.Loading,
    val category: ActivityCategory? = null,
) {
    val categories: List<ActivityCategory>
        get() = (destination as? LoadState.Ready)?.data?.attractions?.map { it.category }?.distinct().orEmpty()
}

sealed interface DestinationExtrasAction {
    data class SelectCategory(val category: ActivityCategory?) : DestinationExtrasAction
    data object Retry : DestinationExtrasAction
}

/** State for the "Things to do" screen of one destination. Live places are handed off to Google Maps. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = DestinationExtrasViewModel.Factory::class)
class DestinationExtrasViewModel @AssistedInject constructor(
    @Assisted private val destinationId: String,
    destinations: DestinationRepository,
) : ViewModel() {
    @AssistedFactory interface Factory { fun create(destinationId: String): DestinationExtrasViewModel }

    private val refresh = MutableStateFlow(0)
    private val category = MutableStateFlow<ActivityCategory?>(null)

    val uiState: StateFlow<DestinationExtrasUiState> = combine(
        refresh.flatMapLatest { destinations.observeDestination(destinationId).asLoadState() },
        category,
    ) { d, c -> DestinationExtrasUiState(d, c) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DestinationExtrasUiState())

    fun onAction(action: DestinationExtrasAction) {
        when (action) {
            is DestinationExtrasAction.SelectCategory -> category.value = action.category
            DestinationExtrasAction.Retry -> refresh.value++
        }
    }
}
