package com.tripwise.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.tripwise.core.designsystem.theme.TripWiseTheme

/**
 * Rounded progress bar. Over 100% it switches to the error color (e.g. over budget)
 * while the bar itself stays full. [description] is read by screen readers.
 */
@Composable
fun TwProgressBar(
    fraction: Float,
    description: String,
    modifier: Modifier = Modifier,
) {
    val over = fraction > 1f
    LinearProgressIndicator(
        progress = { fraction.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .semantics {
                contentDescription = description
                progressBarRangeInfo = ProgressBarRangeInfo(fraction.coerceIn(0f, 1f), 0f..1f)
            },
        color = if (over) TripWiseTheme.colors.error else TripWiseTheme.colors.secondary,
        trackColor = TripWiseTheme.colors.surfaceContainerHighest,
        strokeCap = StrokeCap.Round,
    )
}

@PreviewLightDark
@Composable
private fun TwProgressBarPreview() = TripWiseTheme {
    androidx.compose.foundation.layout.Column {
        TwProgressBar(0.62f, "62% of budget")
        TwProgressBar(1.2f, "Over budget", Modifier.padding(top = 8.dp))
    }
}
