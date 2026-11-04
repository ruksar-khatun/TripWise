package com.tripwise.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.common.result.asLoadState
import com.tripwise.core.domain.repository.AuthRepository
import com.tripwise.core.domain.repository.UserRepository
import com.tripwise.core.domain.usecase.ObserveTravelStatsUseCase
import com.tripwise.core.model.TravelStats
import com.tripwise.core.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileContent(val profile: UserProfile, val stats: TravelStats)

enum class ProfileDialog { SIGN_OUT, DELETE_ACCOUNT }

data class ProfileUiState(
    val content: LoadState<ProfileContent> = LoadState.Loading,
    val dialog: ProfileDialog? = null,
    val busy: Boolean = false,
    val deleteError: AppError? = null,
)

sealed interface ProfileAction {
    data class OpenDialog(val dialog: ProfileDialog) : ProfileAction
    data object DismissDialog : ProfileAction
    data object ConfirmSignOut : ProfileAction
    data class ConfirmDelete(val password: String) : ProfileAction
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    userRepository: UserRepository,
    observeStats: ObserveTravelStatsUseCase,
    private val auth: AuthRepository,
) : ViewModel() {

    private val ui = MutableStateFlow(ProfileUiState())

    val uiState: StateFlow<ProfileUiState> = combine(
        combine(userRepository.observeProfile(), observeStats()) { p, s -> ProfileContent(p, s) }.asLoadState(),
        ui,
    ) { content, ui -> ui.copy(content = content) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.OpenDialog -> ui.update { it.copy(dialog = action.dialog, deleteError = null) }
            ProfileAction.DismissDialog -> ui.update { if (it.busy) it else it.copy(dialog = null, deleteError = null) }
            // Session change swaps the whole UI to the sign-in screen; nothing else to do here.
            ProfileAction.ConfirmSignOut -> launchBusy { auth.signOut() }
            is ProfileAction.ConfirmDelete -> launchBusy {
                val result = auth.deleteAccount(action.password)
                if (result is Outcome.Failure) ui.update { it.copy(deleteError = result.error) }
            }
        }
    }

    private fun launchBusy(block: suspend () -> Unit) {
        if (ui.value.busy) return
        ui.update { it.copy(busy = true, deleteError = null) }
        viewModelScope.launch {
            try { block() } finally { ui.update { it.copy(busy = false) } }
        }
    }
}
