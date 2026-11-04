package com.tripwise.feature.trips.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.common.result.asLoadState
import com.tripwise.core.domain.repository.TripRepository
import com.tripwise.core.model.BudgetSummary
import com.tripwise.core.model.Trip
import com.tripwise.core.model.TripStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

enum class TripsTab { UPCOMING, PAST }

data class TripListItem(val trip: Trip, val budget: BudgetSummary?)

data class TripsUiState(
    val today: LocalDate,
    val tab: TripsTab = TripsTab.UPCOMING,
    val trips: LoadState<List<TripListItem>> = LoadState.Loading,
)

sealed interface TripsAction {
    data class TabSelected(val tab: TripsTab) : TripsAction
    data object Retry : TripsAction
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TripsViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    clock: Clock,
) : ViewModel() {
    private val today = LocalDate.now(clock)
    private val tab = MutableStateFlow(TripsTab.UPCOMING)
    private val refresh = MutableStateFlow(0)

    private val items = refresh.flatMapLatest {
        tripRepository.observeTrips().flatMapLatest { trips ->
            if (trips.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(trips.map { t -> tripRepository.observeBudget(t.id) }) { budgets ->
                    trips.mapIndexed { i, t -> TripListItem(t, budgets[i]) }
                }
            }
        }.asLoadState()
    }

    val uiState: StateFlow<TripsUiState> = combine(tab, items) { tab, items ->
        val filtered = if (items is LoadState.Ready) {
            LoadState.Ready(
                when (tab) {
                    // Ongoing trips stay under Upcoming; past trips are newest first.
                    TripsTab.UPCOMING -> items.data.filter { it.trip.statusOn(today) != TripStatus.PAST }
                    TripsTab.PAST -> items.data.filter { it.trip.statusOn(today) == TripStatus.PAST }.sortedByDescending { it.trip.startDate }
                },
            )
        } else {
            items
        }
        TripsUiState(today, tab, filtered)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TripsUiState(today))

    fun onAction(action: TripsAction) {
        when (action) {
            is TripsAction.TabSelected -> tab.value = action.tab
            TripsAction.Retry -> refresh.value++
        }
    }
}
