package com.tripwise.feature.profile

import com.tripwise.core.model.isGuest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.designsystem.component.TwListRow
import com.tripwise.core.designsystem.component.TwSkeleton
import com.tripwise.core.designsystem.component.TwStatTile
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.model.TravelStats
import com.tripwise.core.ui.error.messageRes
import com.tripwise.core.ui.preview.PreviewData
import com.tripwise.core.ui.state.ErrorState

@Composable
internal fun ProfileScreen(appVersion: String, viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreen(state, viewModel::onAction, appVersion)
}

@Composable
internal fun ProfileScreen(state: ProfileUiState, onAction: (ProfileAction) -> Unit, appVersion: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = 640.dp).fillMaxWidth()) {
            Text(
                stringResource(R.string.feature_profile_title),
                style = TripWiseTheme.typography.headlineMedium,
                color = TripWiseTheme.colors.primary,
                modifier = Modifier.padding(TripWiseTheme.spacing.lg).semantics { heading() },
            )
            when (val content = state.content) {
                LoadState.Loading -> Column(Modifier.fillMaxWidth().padding(TripWiseTheme.spacing.lg), horizontalAlignment = Alignment.CenterHorizontally) {
                    TwSkeleton(Modifier.size(96.dp), height = 96.dp, shape = CircleShape)
                    TwSkeleton(Modifier.padding(top = 16.dp).fillMaxWidth(0.5f), height = 24.dp)
                }
                is LoadState.Failed -> ErrorState(content.error, onRetry = {})
                is LoadState.Ready -> Header(content.data)
            }
            val profile = (state.content as? LoadState.Ready)?.data?.profile
            Settings(appVersion, profile?.homeCurrency?.code, guest = profile?.isGuest == true, onAction)
        }
    }

    when (state.dialog) {
        ProfileDialog.SIGN_OUT -> AlertDialog(
            onDismissRequest = { onAction(ProfileAction.DismissDialog) },
            title = { Text(stringResource(R.string.feature_profile_sign_out_title)) },
            text = { Text(stringResource(R.string.feature_profile_sign_out_body)) },
            confirmButton = {
                TextButton(onClick = { onAction(ProfileAction.ConfirmSignOut) }, enabled = !state.busy) {
                    Text(stringResource(R.string.feature_profile_sign_out))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ProfileAction.DismissDialog) }, enabled = !state.busy) {
                    Text(stringResource(R.string.feature_profile_cancel))
                }
            },
        )
        ProfileDialog.DELETE_ACCOUNT -> DeleteAccountDialog(state, onAction)
        ProfileDialog.EXIT_GUEST -> AlertDialog(
            onDismissRequest = { onAction(ProfileAction.DismissDialog) },
            title = { Text(stringResource(R.string.feature_profile_exit_guest_title)) },
            text = { Text(stringResource(R.string.feature_profile_exit_guest_body)) },
            confirmButton = {
                TextButton(onClick = { onAction(ProfileAction.ConfirmSignOut) }, enabled = !state.busy) {
                    Text(stringResource(R.string.feature_profile_exit_guest))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ProfileAction.DismissDialog) }, enabled = !state.busy) {
                    Text(stringResource(R.string.feature_profile_cancel))
                }
            },
        )
        null -> Unit
    }
}

@Composable
private fun DeleteAccountDialog(state: ProfileUiState, onAction: (ProfileAction) -> Unit) {
    var password by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { onAction(ProfileAction.DismissDialog) },
        icon = { androidx.compose.material3.Icon(TwIcons.Delete, contentDescription = null, tint = TripWiseTheme.colors.error) },
        title = { Text(stringResource(R.string.feature_profile_delete_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md)) {
                Text(stringResource(R.string.feature_profile_delete_body))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.feature_profile_password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = state.deleteError != null,
                    supportingText = state.deleteError?.let { { Text(stringResource(it.messageRes())) } },
                    enabled = !state.busy,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAction(ProfileAction.ConfirmDelete(password)) },
                enabled = password.isNotEmpty() && !state.busy,
            ) { Text(stringResource(R.string.feature_profile_delete_confirm), color = TripWiseTheme.colors.error) }
        },
        dismissButton = {
            TextButton(onClick = { onAction(ProfileAction.DismissDialog) }, enabled = !state.busy) {
                Text(stringResource(R.string.feature_profile_cancel))
            }
        },
    )
}

@Composable
private fun Header(content: ProfileContent) {
    val p = content.profile
    Column(Modifier.fillMaxWidth().padding(horizontal = TripWiseTheme.spacing.lg), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(Modifier.size(96.dp), shape = CircleShape, color = TripWiseTheme.colors.primaryContainer) {
            Box(contentAlignment = Alignment.Center) {
                Text(initials(p.displayName), style = TripWiseTheme.typography.headlineMedium, color = TripWiseTheme.colors.onPrimaryContainer)
            }
        }
        Text(p.displayName, style = TripWiseTheme.typography.titleLarge, modifier = Modifier.padding(top = TripWiseTheme.spacing.md))
        Text(p.email, style = TripWiseTheme.typography.bodyMedium, color = TripWiseTheme.colors.onSurfaceVariant)
        Stats(content.stats, Modifier.padding(vertical = TripWiseTheme.spacing.lg))
    }
}

@Composable
private fun Stats(stats: TravelStats, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md)) {
        TwStatTile(stats.trips.toString(), stringResource(R.string.feature_profile_trips), Modifier.weight(1f), TwIcons.Trips)
        TwStatTile(stats.saved.toString(), stringResource(R.string.feature_profile_saved), Modifier.weight(1f), TwIcons.Saved)
        TwStatTile(stats.countries.toString(), stringResource(R.string.feature_profile_countries), Modifier.weight(1f), TwIcons.Place)
    }
}

@Composable
private fun Settings(appVersion: String, currency: String?, guest: Boolean, onAction: (ProfileAction) -> Unit) {
    val later = stringResource(R.string.feature_profile_later_phase)
    SectionTitle(stringResource(R.string.feature_profile_preferences))
    TwListRow(stringResource(R.string.feature_profile_theme), icon = TwIcons.Theme,
        trailing = stringResource(R.string.feature_profile_theme_system), subtitle = later, enabled = false, onClick = {})
    TwListRow(stringResource(R.string.feature_profile_currency), icon = TwIcons.Currency,
        trailing = currency, subtitle = later, enabled = false, onClick = {})
    HorizontalDivider(Modifier.padding(vertical = TripWiseTheme.spacing.sm))
    SectionTitle(stringResource(R.string.feature_profile_account))
    if (guest) {
        TwListRow(
            stringResource(R.string.feature_profile_create_account), icon = TwIcons.Profile,
            subtitle = stringResource(R.string.feature_profile_create_account_subtitle),
            onClick = { onAction(ProfileAction.CreateAccount) },
        )
        TwListRow(
            stringResource(R.string.feature_profile_exit_guest), icon = TwIcons.SignOut,
            subtitle = stringResource(R.string.feature_profile_exit_guest_subtitle),
            onClick = { onAction(ProfileAction.OpenDialog(ProfileDialog.EXIT_GUEST)) },
        )
    } else {
    TwListRow(stringResource(R.string.feature_profile_sign_out), icon = TwIcons.SignOut,
        onClick = { onAction(ProfileAction.OpenDialog(ProfileDialog.SIGN_OUT)) })
    TwListRow(
        stringResource(R.string.feature_profile_delete_account), icon = TwIcons.Delete,
        subtitle = stringResource(R.string.feature_profile_delete_subtitle),
        contentColor = TripWiseTheme.colors.error,
        onClick = { onAction(ProfileAction.OpenDialog(ProfileDialog.DELETE_ACCOUNT)) },
    )
    }
    HorizontalDivider(Modifier.padding(vertical = TripWiseTheme.spacing.sm))
    SectionTitle(stringResource(R.string.feature_profile_about))
    TwListRow(stringResource(R.string.feature_profile_version, appVersion), icon = TwIcons.Info)
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = TripWiseTheme.typography.titleSmall,
        color = TripWiseTheme.colors.secondary,
        modifier = Modifier.padding(start = TripWiseTheme.spacing.lg, top = TripWiseTheme.spacing.md, bottom = TripWiseTheme.spacing.xs)
            .semantics { heading() },
    )
}

internal fun initials(name: String): String =
    name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }

@PreviewLightDark
@Composable
private fun ProfilePreview() = TripWiseTheme {
    Surface { ProfileScreen(ProfileUiState(LoadState.Ready(ProfileContent(PreviewData.profile, TravelStats(3, 12, 5)))), {}, "0.1.0") }
}
