package com.tripwise.feature.explore

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.tripwise.core.designsystem.component.TwTopAppBar
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.ui.state.EmptyState

/**
 * Placeholder tab so the navigation shell is complete. Replaced by the real
 * Route/ViewModel/Screen trio when this feature's phase is approved.
 */
@Composable
internal fun ExplorePlaceholderScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = { TwTopAppBar(title = stringResource(R.string.feature_explore_title), centered = true) },
    ) { padding ->
        EmptyState(
            title = stringResource(R.string.feature_explore_placeholder_title),
            body = stringResource(R.string.feature_explore_placeholder_body),
            icon = TwIcons.Explore,
            modifier = Modifier.padding(padding),
        )
    }
}

@PreviewLightDark
@Composable
private fun ExplorePlaceholderScreenPreview() = TripWiseTheme { ExplorePlaceholderScreen() }
