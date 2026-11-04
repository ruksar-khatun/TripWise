package com.tripwise.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.core.common.error.AppError
import com.tripwise.core.designsystem.component.TwCard
import com.tripwise.core.designsystem.component.TwCardStyle
import com.tripwise.core.designsystem.component.TwOutlinedButton
import com.tripwise.core.designsystem.component.TwSectionHeader
import com.tripwise.core.designsystem.component.TwSkeleton
import com.tripwise.core.designsystem.component.TwTopAppBar
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.model.BackendStatus
import com.tripwise.core.ui.error.messageRes

/** Stateful entry: obtains the ViewModel and wires state + actions into the stateless screen. */
@Composable
internal fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(state = state, onAction = viewModel::onAction)
}

/** Stateless screen: everything it shows comes from [state]; previews and UI tests target this. */
@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TwTopAppBar(title = stringResource(R.string.feature_home_title), centered = true) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(TripWiseTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.lg),
        ) {
            TwSectionHeader(stringResource(R.string.feature_home_welcome_title))
            Text(
                stringResource(R.string.feature_home_welcome_body),
                style = TripWiseTheme.typography.bodyLarge,
                color = TripWiseTheme.colors.onSurfaceVariant,
            )
            BackendStatusCard(state.backend, onRetry = { onAction(HomeAction.RetryBackendCheck) })
        }
    }
}

@Composable
private fun BackendStatusCard(check: BackendCheck, onRetry: () -> Unit) {
    TwCard(Modifier.fillMaxWidth(), style = TwCardStyle.Outlined) {
        Text(stringResource(R.string.feature_home_backend_title), style = TripWiseTheme.typography.titleMedium)
        Column(
            Modifier.padding(top = TripWiseTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
        ) {
            when (check) {
                BackendCheck.Checking -> {
                    Text(stringResource(R.string.feature_home_backend_checking), style = TripWiseTheme.typography.bodyMedium)
                    TwSkeleton(Modifier.fillMaxWidth(0.6f))
                }
                is BackendCheck.Connected -> Text(
                    if (check.status.isHealthy) {
                        stringResource(R.string.feature_home_backend_connected, check.status.service, check.status.version)
                    } else {
                        stringResource(R.string.feature_home_backend_unhealthy)
                    },
                    style = TripWiseTheme.typography.bodyMedium,
                )
                is BackendCheck.Failed -> {
                    Text(
                        stringResource(check.error.messageRes()),
                        style = TripWiseTheme.typography.bodyMedium,
                        color = TripWiseTheme.colors.error,
                    )
                    TwOutlinedButton(stringResource(R.string.feature_home_backend_check_again), onRetry)
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun HomeConnectedPreview() = TripWiseTheme {
    HomeScreen(HomeUiState(BackendCheck.Connected(BackendStatus("tripwise-backend", "0.1.0", true))), onAction = {})
}

@PreviewLightDark
@Composable
private fun HomeFailedPreview() = TripWiseTheme {
    HomeScreen(HomeUiState(BackendCheck.Failed(AppError.Network)), onAction = {})
}
