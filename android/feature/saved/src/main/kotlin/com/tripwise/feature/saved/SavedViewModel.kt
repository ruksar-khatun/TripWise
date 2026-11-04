package com.tripwise.feature.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.common.result.asLoadState
import com.tripwise.core.domain.repository.SavedDestinationRepository
import com.tripwise.core.model.SavedDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SavedSort { RECENT, NAME }

data class SavedUiState(
    val sort: SavedSort = SavedSort.RECENT,
    val items: LoadState<List<SavedDestination>> = LoadState.Loading,
    /** Last removed item; the screen shows an Undo snackbar while this is set. */
    val lastRemoved: SavedDestination? = null,
)

sealed interface SavedAction {
    data class SortChanged(val sort: SavedSort) : SavedAction
    data class Remove(val item: SavedDestination) : SavedAction
    data object UndoRemove : SavedAction
    data object UndoDismissed : SavedAction
    data object Retry : SavedAction
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SavedViewModel @Inject constructor(
    private val repository: SavedDestinationRepository,
) : ViewModel() {

    private val sort = MutableStateFlow(SavedSort.RECENT)
    private val lastRemoved = MutableStateFlow<SavedDestination?>(null)
    private val refresh = MutableStateFlow(0)

    private val items = refresh.flatMapLatest { repository.observeSaved().asLoadState() }

    val uiState: StateFlow<SavedUiState> = combine(sort, items, lastRemoved) { sort, items, removed ->
        val sorted = if (items is LoadState.Ready) {
            LoadState.Ready(
                when (sort) {
                    SavedSort.RECENT -> items.data.sortedByDescending { it.savedAt }
                    SavedSort.NAME -> items.data.sortedBy { it.destination.name.lowercase() }
                },
            )
        } else {
            items
        }
        SavedUiState(sort, sorted, removed)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SavedUiState())

    fun onAction(action: SavedAction) {
        when (action) {
            is SavedAction.SortChanged -> sort.value = action.sort
            is SavedAction.Remove -> viewModelScope.launch {
                lastRemoved.value = action.item
                repository.setSaved(action.item.destination.id, false)
            }
            SavedAction.UndoRemove -> viewModelScope.launch {
                lastRemoved.value?.let { repository.setSaved(it.destination.id, true) }
                lastRemoved.value = null
            }
            SavedAction.UndoDismissed -> lastRemoved.update { null }
            SavedAction.Retry -> refresh.value++
        }
    }
}
