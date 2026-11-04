package com.tripwise.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.domain.repository.SystemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Reference ViewModel for the project's state pattern:
 *  - private MutableStateFlow, public read-only StateFlow<UiState>
 *  - repositories return Outcome, never throw
 *  - user intents arrive via onAction(HomeAction)
 * Screens backed by Room will instead build state with combine(...).stateIn(WhileSubscribed(5_000)).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val systemRepository: SystemRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var checkJob: Job? = null

    init {
        checkBackend()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.RetryBackendCheck -> checkBackend()
        }
    }

    private fun checkBackend() {
        checkJob?.cancel()
        _uiState.update { it.copy(backend = BackendCheck.Checking) }
        checkJob = viewModelScope.launch {
            val backend = when (val result = systemRepository.checkBackend()) {
                is Outcome.Success -> BackendCheck.Connected(result.value)
                is Outcome.Failure -> BackendCheck.Failed(result.error)
            }
            _uiState.update { it.copy(backend = backend) }
        }
    }
}
