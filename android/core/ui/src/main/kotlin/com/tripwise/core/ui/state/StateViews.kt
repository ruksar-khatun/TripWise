package com.tripwise.core.ui.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.error.isRetryable
import com.tripwise.core.designsystem.component.TwButton
import com.tripwise.core.designsystem.component.TwSkeletonCard
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.ui.R
import com.tripwise.core.ui.error.messageRes

/** Full-area loading. Prefer [SkeletonListState] for lists so the layout doesn't jump. */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.core_ui_loading)
    Box(modifier.fillMaxSize().semantics { contentDescription = description }, Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** Skeleton placeholder for card lists. */
@Composable
fun SkeletonListState(modifier: Modifier = Modifier, itemCount: Int = 3) {
    val description = stringResource(R.string.core_ui_loading)
    Column(
        modifier.fillMaxWidth().padding(TripWiseTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.xl),
    ) {
        repeat(itemCount) { index ->
            TwSkeletonCard(loadingDescription = if (index == 0) description else null)
        }
    }
}

/** Empty state with a clear next step. Every list screen defines its own copy. */
@Composable
fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = TwIcons.Empty,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    MessageLayout(icon, title, body, modifier, actionLabel, onAction)
}

/** Error state for when there is no cached content to show. Retry is offered only for retryable errors. */
@Composable
fun ErrorState(
    error: AppError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(if (error == AppError.NotConfigured) R.string.core_ui_not_available_title else R.string.core_ui_error_title),
) {
    MessageLayout(
        icon = when (error) {
            AppError.Network -> TwIcons.Offline
            AppError.NotConfigured -> TwIcons.Info
            else -> TwIcons.Error
        },
        title = title,
        body = stringResource(error.messageRes()),
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        actionLabel = if (error.isRetryable) stringResource(R.string.core_ui_retry) else null,
        onAction = if (error.isRetryable) onRetry else null,
    )
}

@Composable
private fun MessageLayout(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier,
    actionLabel: String?,
    onAction: (() -> Unit)?,
) {
    Column(
        modifier.fillMaxSize().padding(TripWiseTheme.spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, Modifier.size(56.dp), tint = TripWiseTheme.colors.primary)
        Text(title, style = TripWiseTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(
            body,
            style = TripWiseTheme.typography.bodyMedium,
            color = TripWiseTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            TwButton(actionLabel, onAction, Modifier.padding(top = TripWiseTheme.spacing.sm))
        }
    }
}

/** Thin banner shown by the app shell while offline. */
@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), color = TripWiseTheme.colors.secondaryContainer) {
        Row(
            Modifier
                .padding(horizontal = TripWiseTheme.spacing.lg, vertical = TripWiseTheme.spacing.sm)
                .semantics { liveRegion = LiveRegionMode.Polite },
            horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(TwIcons.Offline, contentDescription = null, Modifier.size(18.dp))
            Text(stringResource(R.string.core_ui_offline_banner), style = TripWiseTheme.typography.labelLarge)
        }
    }
}

@PreviewLightDark
@Composable
private fun EmptyStatePreview() = TripWiseTheme {
    Surface { EmptyState("No trips yet", "Plan your first trip to see it here.", actionLabel = "Plan a trip", onAction = {}) }
}

@PreviewLightDark
@Composable
private fun ErrorStatePreview() = TripWiseTheme {
    Surface { ErrorState(AppError.Network, onRetry = {}) }
}

@PreviewLightDark
@Composable
private fun OfflineBannerPreview() = TripWiseTheme { OfflineBanner() }

/** Inline error for one section of a multi-section screen, so the rest stays usable. */
@Composable
fun SectionErrorCard(error: AppError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier.fillMaxWidth(),
        shape = TripWiseTheme.shapes.card,
        color = TripWiseTheme.colors.errorContainer,
        contentColor = TripWiseTheme.colors.onErrorContainer,
    ) {
        Row(
            Modifier.padding(TripWiseTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(TwIcons.Error, contentDescription = null)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.core_ui_section_retry_title), style = TripWiseTheme.typography.titleSmall)
                Text(stringResource(error.messageRes()), style = TripWiseTheme.typography.bodySmall)
            }
            if (error.isRetryable) {
                com.tripwise.core.designsystem.component.TwTextButton(stringResource(R.string.core_ui_retry), onRetry)
            }
        }
    }
}
