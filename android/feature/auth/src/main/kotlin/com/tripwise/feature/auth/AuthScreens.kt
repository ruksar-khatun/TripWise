package com.tripwise.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.core.common.error.AppError
import com.tripwise.core.designsystem.component.TwButton
import com.tripwise.core.designsystem.component.TwTextButton
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.BrandColors
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.ui.error.messageRes

@Composable
internal fun SignInScreen(sessionExpired: Boolean, onCreateAccount: () -> Unit, viewModel: SignInViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AuthScreen(signUp = false, state, viewModel::onAction, onSwitch = onCreateAccount, sessionExpired = sessionExpired)
}

@Composable
internal fun SignUpScreen(onSignIn: () -> Unit, viewModel: SignUpViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AuthScreen(signUp = true, state, viewModel::onAction, onSwitch = onSignIn)
}

@Composable
private fun FieldProblem.message(): String = stringResource(
    when (this) {
        FieldProblem.EMAIL_INVALID -> R.string.feature_auth_error_email
        FieldProblem.PASSWORD_REQUIRED -> R.string.feature_auth_error_password_required
        FieldProblem.PASSWORD_SHORT -> R.string.feature_auth_error_password_short
        FieldProblem.NAME_REQUIRED -> R.string.feature_auth_error_name
    },
)

@Composable
internal fun AuthScreen(
    signUp: Boolean,
    state: AuthFormState,
    onAction: (AuthAction) -> Unit,
    onSwitch: () -> Unit,
    sessionExpired: Boolean = false,
) {
    val focus = LocalFocusManager.current
    fun errorFor(field: AuthField): String? = state.serverFieldErrors[field]
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).safeDrawingPadding().imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BrandHeader()
            Column(
                Modifier.widthIn(max = 480.dp).fillMaxWidth().padding(TripWiseTheme.spacing.xl),
                verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md),
            ) {
                Text(
                    stringResource(if (signUp) R.string.feature_auth_sign_up_title else R.string.feature_auth_sign_in_title),
                    style = TripWiseTheme.typography.headlineMedium,
                    color = TripWiseTheme.colors.primary,
                    modifier = Modifier.semantics { heading() },
                )
                if (sessionExpired && !signUp) Banner(stringResource(R.string.feature_auth_session_expired), isError = false)
                state.formError?.let { Banner(stringResource(it.messageRes()), isError = true) }

                if (signUp) {
                    AuthField(
                        value = state.name, onValueChange = { onAction(AuthAction.NameChanged(it)) },
                        label = stringResource(R.string.feature_auth_name), icon = TwIcons.Profile,
                        error = state.problems[AuthField.NAME]?.message() ?: errorFor(AuthField.NAME),
                        keyboard = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                        contentType = ContentType.PersonFullName,
                        onIme = { focus.moveFocus(FocusDirection.Down) },
                    )
                }
                AuthField(
                    value = state.email, onValueChange = { onAction(AuthAction.EmailChanged(it)) },
                    label = stringResource(R.string.feature_auth_email), icon = TwIcons.Email,
                    error = state.problems[AuthField.EMAIL]?.message() ?: errorFor(AuthField.EMAIL),
                    keyboard = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    contentType = ContentType.EmailAddress,
                    onIme = { focus.moveFocus(FocusDirection.Down) },
                )
                AuthField(
                    value = state.password, onValueChange = { onAction(AuthAction.PasswordChanged(it)) },
                    label = stringResource(R.string.feature_auth_password), icon = TwIcons.Lock,
                    error = state.problems[AuthField.PASSWORD]?.message() ?: errorFor(AuthField.PASSWORD),
                    supporting = if (signUp) stringResource(R.string.feature_auth_password_hint) else null,
                    keyboard = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    contentType = if (signUp) ContentType.NewPassword else ContentType.Password,
                    visual = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailing = {
                        IconButton(onClick = { onAction(AuthAction.TogglePasswordVisibility) }) {
                            Icon(
                                if (state.passwordVisible) TwIcons.VisibilityOff else TwIcons.Visibility,
                                stringResource(if (state.passwordVisible) R.string.feature_auth_hide_password else R.string.feature_auth_show_password),
                            )
                        }
                    },
                    onIme = { focus.clearFocus(); onAction(AuthAction.Submit) },
                )
                TwButton(
                    stringResource(if (signUp) R.string.feature_auth_sign_up else R.string.feature_auth_sign_in),
                    onClick = { focus.clearFocus(); onAction(AuthAction.Submit) },
                    loading = state.submitting,
                    modifier = Modifier.fillMaxWidth().padding(top = TripWiseTheme.spacing.sm),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(if (signUp) R.string.feature_auth_have_account else R.string.feature_auth_no_account),
                        style = TripWiseTheme.typography.bodyMedium,
                        color = TripWiseTheme.colors.onSurfaceVariant,
                    )
                    TwTextButton(
                        stringResource(if (signUp) R.string.feature_auth_back_to_sign_in else R.string.feature_auth_create_one),
                        onClick = onSwitch,
                        enabled = !state.submitting,
                    )
                }
                if (signUp) {
                    Text(
                        stringResource(R.string.feature_auth_terms),
                        style = TripWiseTheme.typography.bodySmall,
                        color = TripWiseTheme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun BrandHeader() {
    Box(
        Modifier
            .fillMaxWidth()
            .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(BrandColors.DuskBlue, BrandColors.SeaBlue)))
            .padding(vertical = TripWiseTheme.spacing.xxl),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // The logo carries the "TripWise" wordmark, so it is the brand title for accessibility too.
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(com.tripwise.core.designsystem.R.drawable.tripwise_logo),
                contentDescription = stringResource(R.string.feature_auth_brand),
                modifier = Modifier.size(128.dp).semantics { heading() },
            )
            Text(
                stringResource(R.string.feature_auth_tagline),
                style = TripWiseTheme.typography.bodyMedium,
                color = BrandColors.AzureMist,
                modifier = Modifier.padding(top = TripWiseTheme.spacing.md),
            )
        }
    }
}

@Composable
private fun Banner(text: String, isError: Boolean) {
    Surface(
        shape = TripWiseTheme.shapes.field,
        color = if (isError) TripWiseTheme.colors.errorContainer else TripWiseTheme.colors.secondaryContainer,
        contentColor = if (isError) TripWiseTheme.colors.onErrorContainer else TripWiseTheme.colors.onSecondaryContainer,
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Row(Modifier.padding(TripWiseTheme.spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (isError) TwIcons.Error else TwIcons.Info, contentDescription = null)
            Text(text, style = TripWiseTheme.typography.bodyMedium, modifier = Modifier.padding(start = TripWiseTheme.spacing.md))
        }
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    error: String?,
    keyboard: KeyboardOptions,
    contentType: ContentType,
    onIme: () -> Unit,
    supporting: String? = null,
    visual: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        trailingIcon = trailing,
        isError = error != null,
        supportingText = (error ?: supporting)?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = keyboard,
        keyboardActions = KeyboardActions(onAny = { onIme() }),
        visualTransformation = visual,
        shape = TripWiseTheme.shapes.field,
        // Lets password managers fill and save credentials.
        modifier = Modifier.fillMaxWidth().semantics { this.contentType = contentType },
    )
}

@PreviewLightDark
@Composable
private fun SignInPreview() = TripWiseTheme {
    AuthScreen(false, AuthFormState(email = "alex@", formError = AppError.InvalidCredentials), {}, {}, sessionExpired = true)
}

@PreviewLightDark
@Composable
private fun SignUpPreview() = TripWiseTheme {
    AuthScreen(true, AuthFormState(problems = mapOf(AuthField.PASSWORD to FieldProblem.PASSWORD_SHORT)), {}, {})
}
