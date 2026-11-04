package com.tripwise.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme

/** Value + label tile (e.g. "7 · days"). Read as one unit by screen readers. */
@Composable
fun TwStatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Surface(
        modifier.semantics(mergeDescendants = true) {},
        shape = TripWiseTheme.shapes.card,
        color = TripWiseTheme.colors.surfaceContainerLowest,
    ) {
        Column(Modifier.padding(TripWiseTheme.spacing.md)) {
            if (icon != null) {
                Icon(icon, contentDescription = null, Modifier.size(20.dp), tint = TripWiseTheme.colors.secondary)
            }
            Text(
                value,
                style = TripWiseTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = TripWiseTheme.spacing.xs),
            )
            Text(label, style = TripWiseTheme.typography.labelMedium, color = TripWiseTheme.colors.onSurfaceVariant)
        }
    }
}

@PreviewLightDark
@Composable
private fun TwStatTilePreview() = TripWiseTheme { TwStatTile("7", "days", icon = TwIcons.Calendar) }
