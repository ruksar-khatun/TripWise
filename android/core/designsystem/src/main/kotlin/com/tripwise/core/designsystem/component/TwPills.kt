package com.tripwise.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme

enum class TwPillTone { Neutral, Accent, Success, OnImage }

/** Compact icon + label pill for quick facts, countdowns and ratings. */
@Composable
fun TwInfoPill(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    tone: TwPillTone = TwPillTone.Neutral,
) {
    val (container, content) = when (tone) {
        TwPillTone.Neutral -> TripWiseTheme.colors.surfaceContainerHigh to TripWiseTheme.colors.onSurface
        TwPillTone.Accent -> TripWiseTheme.colors.secondaryContainer to TripWiseTheme.colors.onSecondaryContainer
        TwPillTone.Success -> TripWiseTheme.colors.tertiaryContainer to TripWiseTheme.colors.onTertiaryContainer
        TwPillTone.OnImage -> TripWiseTheme.extendedColors.overlayButton to TripWiseTheme.colors.onSurface
    }
    Surface(modifier, shape = TripWiseTheme.shapes.pill, color = container, contentColor = content) {
        Row(
            Modifier.padding(horizontal = TripWiseTheme.spacing.md, vertical = TripWiseTheme.spacing.xs + 2.dp),
            horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) Icon(icon, contentDescription = null, Modifier.size(16.dp), tint = iconTint ?: content)
            Text(text, style = TripWiseTheme.typography.labelMedium)
        }
    }
}

/** Star rating pill. [description] should read e.g. "Rated 4.7 out of 5". */
@Composable
fun TwRatingPill(rating: Double, description: String, modifier: Modifier = Modifier, tone: TwPillTone = TwPillTone.Neutral) {
    TwInfoPill(
        text = "%.1f".format(rating),
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        icon = TwIcons.Star,
        iconTint = TripWiseTheme.extendedColors.rating,
        tone = tone,
    )
}

@PreviewLightDark
@Composable
private fun TwPillsPreview() = TripWiseTheme {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TwInfoPill("Apr – Oct", icon = TwIcons.Sun)
        TwInfoPill("in 12 days", tone = TwPillTone.Success)
        TwRatingPill(4.7, "Rated 4.7")
    }
}
