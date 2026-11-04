package com.tripwise.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.common.result.asLoadState
import com.tripwise.core.domain.repository.UserRepository
import com.tripwise.core.domain.usecase.ObserveTravelStatsUseCase
import com.tripwise.core.model.TravelStats
import com.tripwise.core.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ProfileContent(val profile: UserProfile, val stats: TravelStats)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    userRepository: UserRepository,
    observeStats: ObserveTravelStatsUseCase,
) : ViewModel() {
    val uiState: StateFlow<LoadState<ProfileContent>> =
        combine(userRepository.observeProfile(), observeStats()) { p, s -> ProfileContent(p, s) }
            .asLoadState()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LoadState.Loading)
}
