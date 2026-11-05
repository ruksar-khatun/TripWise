package com.tripwise.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Shared logic for both forms. On success the session flips to SignedIn and the app shell swaps
 * the auth graph for the main app, so no navigation event is needed here.
 */
abstract class AuthFormViewModel(private val signUp: Boolean) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthFormState())
    val uiState: StateFlow<AuthFormState> = _uiState.asStateFlow()
    private var attempted = false

    protected abstract suspend fun submit(state: AuthFormState): Outcome<Unit>

    fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.NameChanged -> edit { it.copy(name = action.value.take(60), serverFieldErrors = it.serverFieldErrors - AuthField.NAME) }
            is AuthAction.EmailChanged -> edit { it.copy(email = action.value.take(254), serverFieldErrors = it.serverFieldErrors - AuthField.EMAIL) }
            is AuthAction.PasswordChanged -> edit { it.copy(password = action.value.take(128), serverFieldErrors = it.serverFieldErrors - AuthField.PASSWORD) }
            AuthAction.TogglePasswordVisibility -> _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
            AuthAction.Submit -> onSubmit()
        }
    }

    private fun edit(change: (AuthFormState) -> AuthFormState) = _uiState.update { s ->
        val next = change(s).copy(formError = null)
        if (attempted) next.copy(problems = AuthValidation.validate(next, signUp)) else next
    }

    private fun onSubmit() {
        val state = _uiState.value
        if (state.submitting) return
        attempted = true
        val problems = AuthValidation.validate(state, signUp)
        _uiState.update { it.copy(problems = problems, formError = null) }
        if (problems.isNotEmpty()) return

        _uiState.update { it.copy(submitting = true) }
        viewModelScope.launch {
            when (val result = submit(state)) {
                is Outcome.Success -> _uiState.update { it.copy(submitting = false) }
                is Outcome.Failure -> _uiState.update {
                    val error = result.error
                    if (error is AppError.Validation && error.fields.isNotEmpty()) {
                        it.copy(submitting = false, serverFieldErrors = AuthValidation.serverFields(error))
                    } else {
                        it.copy(submitting = false, formError = error)
                    }
                }
            }
        }
    }
}

@HiltViewModel
class SignInViewModel @Inject constructor(private val auth: AuthRepository) : AuthFormViewModel(signUp = false) {
    override suspend fun submit(state: AuthFormState) = auth.signIn(state.email, state.password)

    /** No account or server needed: explore, plan and keep trips on this phone. */
    fun continueAsGuest() {
        android.util.Log.i("Session", "guest: button tapped")
        viewModelScope.launch { auth.continueAsGuest() }
    }
}

@HiltViewModel
class SignUpViewModel @Inject constructor(private val auth: AuthRepository) : AuthFormViewModel(signUp = true) {
    override suspend fun submit(state: AuthFormState) = auth.signUp(state.name, state.email, state.password)
}
