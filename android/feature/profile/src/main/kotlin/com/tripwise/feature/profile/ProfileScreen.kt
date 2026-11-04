package com.tripwise.feature.profile

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
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
import com.tripwise.core.ui.preview.PreviewData
import com.tripwise.core.ui.state.ErrorState

@Composable
internal fun ProfileScreen(appVersion: String, viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreen(state, appVersion)
}

@Composable
internal fun ProfileScreen(state: LoadState<ProfileContent>, appVersion: String, modifier: Modifier = Modifier) {
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
            when (state) {
                LoadState.Loading -> Column(Modifier.padding(TripWiseTheme.spacing.lg), horizontalAlignment = Alignment.CenterHorizontally) {
                    TwSkeleton(Modifier.size(96.dp), height = 96.dp, shape = CircleShape)
                    TwSkeleton(Modifier.padding(top = 16.dp).fillMaxWidth(0.5f), height = 24.dp)
                }
                is LoadState.Failed -> ErrorState(state.error, onRetry = {})
                is LoadState.Ready -> Header(state.data)
            }
            Settings(appVersion, state.dataOrNullCurrency())
        }
    }
}

private fun LoadState<ProfileContent>.dataOrNullCurrency(): String? = (this as? LoadState.Ready)?.data?.profile?.homeCurrency?.code

@Composable
private fun Header(content: ProfileContent) {
    val p = content.profile
    Column(Modifier.fillMaxWidth().padding(horizontal = TripWiseTheme.spacing.lg), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(Modifier.size(96.dp), shape = CircleShape, color = TripWiseTheme.colors.primaryContainer) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    initials(p.displayName),
                    style = TripWiseTheme.typography.headlineMedium,
                    color = TripWiseTheme.colors.onPrimaryContainer,
                )
            }
        }
        Text(p.displayName, style = TripWiseTheme.typography.titleLarge, modifier = Modifier.padding(top = TripWiseTheme.spacing.md))
        Text(p.email, style = TripWiseTheme.typography.bodyMedium, color = TripWiseTheme.colors.onSurfaceVariant)
        Text(
            stringResource(R.string.feature_profile_demo_notice),
            style = TripWiseTheme.typography.bodySmall,
            color = TripWiseTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = TripWiseTheme.spacing.sm),
        )
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
private fun Settings(appVersion: String, currency: String?) {
    val later = stringResource(R.string.feature_profile_later_phase)
    SectionTitle(stringResource(R.string.feature_profile_preferences))
    TwListRow(stringResource(R.string.feature_profile_theme), icon = TwIcons.Theme,
        trailing = stringResource(R.string.feature_profile_theme_system), subtitle = later, enabled = false, onClick = {})
    TwListRow(stringResource(R.string.feature_profile_currency), icon = TwIcons.Currency,
        trailing = currency, subtitle = later, enabled = false, onClick = {})
    HorizontalDivider(Modifier.padding(vertical = TripWiseTheme.spacing.sm))
    SectionTitle(stringResource(R.string.feature_profile_account))
    TwListRow(stringResource(R.string.feature_profile_sign_out), icon = TwIcons.SignOut, subtitle = later, enabled = false, onClick = {})
    TwListRow(
        stringResource(R.string.feature_profile_delete_account), icon = TwIcons.Delete, subtitle = later,
        contentColor = TripWiseTheme.colors.error, enabled = false, onClick = {},
    )
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
    Surface { ProfileScreen(LoadState.Ready(ProfileContent(PreviewData.profile, TravelStats(3, 12, 5))), "0.1.0") }
}
