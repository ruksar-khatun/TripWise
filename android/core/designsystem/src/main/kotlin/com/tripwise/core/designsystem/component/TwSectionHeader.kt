package com.tripwise.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.tripwise.core.designsystem.theme.TripWiseTheme

/** Section title with an optional trailing action (e.g. "See all"). Marked as a heading for TalkBack. */
@Composable
fun TwSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            style = TripWiseTheme.typography.titleLarge,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        if (actionLabel != null && onAction != null) TwTextButton(actionLabel, onAction)
    }
}

@PreviewLightDark
@Composable
private fun TwSectionHeaderPreview() = TripWiseTheme {
    TwSectionHeader("Upcoming trips", actionLabel = "See all", onAction = {})
}
