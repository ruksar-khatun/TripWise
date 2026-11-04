package com.tripwise.feature.auth

import com.tripwise.core.common.error.AppError

enum class AuthField { NAME, EMAIL, PASSWORD }
enum class FieldProblem { EMAIL_INVALID, PASSWORD_REQUIRED, PASSWORD_SHORT, NAME_REQUIRED }

/** Shared form state for sign-in and sign-up. */
data class AuthFormState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val submitting: Boolean = false,
    /** Client-side problems, shown only after the first submit attempt. */
    val problems: Map<AuthField, FieldProblem> = emptyMap(),
    /** Server-side field messages (e.g. "email already registered"). */
    val serverFieldErrors: Map<AuthField, String> = emptyMap(),
    /** Non-field failure (wrong credentials, offline, server error). */
    val formError: AppError? = null,
)

sealed interface AuthAction {
    data class NameChanged(val value: String) : AuthAction
    data class EmailChanged(val value: String) : AuthAction
    data class PasswordChanged(val value: String) : AuthAction
    data object TogglePasswordVisibility : AuthAction
    data object Submit : AuthAction
}

internal object AuthValidation {
    private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    const val MIN_PASSWORD = 8

    fun validate(state: AuthFormState, signUp: Boolean): Map<AuthField, FieldProblem> = buildMap {
        if (signUp && state.name.isBlank()) put(AuthField.NAME, FieldProblem.NAME_REQUIRED)
        if (!EMAIL.matches(state.email.trim())) put(AuthField.EMAIL, FieldProblem.EMAIL_INVALID)
        when {
            state.password.isEmpty() -> put(AuthField.PASSWORD, FieldProblem.PASSWORD_REQUIRED)
            signUp && state.password.length < MIN_PASSWORD -> put(AuthField.PASSWORD, FieldProblem.PASSWORD_SHORT)
        }
    }

    /** Maps server validation keys ("body.email") to form fields. */
    fun serverFields(error: AppError.Validation): Map<AuthField, String> = error.fields.mapNotNull { (key, message) ->
        when (key.substringAfterLast('.')) {
            "email" -> AuthField.EMAIL
            "password" -> AuthField.PASSWORD
            "displayName" -> AuthField.NAME
            else -> null
        }?.let { it to message }
    }.toMap()
}
