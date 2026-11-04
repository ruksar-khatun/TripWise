package com.tripwise.feature.assistant

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
internal fun AssistantPlaceholderScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = { TwTopAppBar(title = stringResource(R.string.feature_assistant_title), centered = true) },
    ) { padding ->
        EmptyState(
            title = stringResource(R.string.feature_assistant_placeholder_title),
            body = stringResource(R.string.feature_assistant_placeholder_body),
            icon = TwIcons.Assistant,
            modifier = Modifier.padding(padding),
        )
    }
}

@PreviewLightDark
@Composable
private fun AssistantPlaceholderScreenPreview() = TripWiseTheme { AssistantPlaceholderScreen() }
