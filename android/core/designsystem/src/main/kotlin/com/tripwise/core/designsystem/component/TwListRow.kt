package com.tripwise.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme

/**
 * Settings-style row: icon, title, optional subtitle and trailing value.
 * Pass [onClick] = null for a read-only row; [enabled] = false dims it (feature not yet available).
 */
@Composable
fun TwListRow(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
    trailing: String? = null,
    contentColor: Color = TripWiseTheme.colors.onSurface,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val alpha = if (enabled) 1f else 0.5f
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier)
            .padding(horizontal = TripWiseTheme.spacing.lg, vertical = TripWiseTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = contentColor.copy(alpha = alpha))
        Column(Modifier.weight(1f)) {
            Text(title, style = TripWiseTheme.typography.bodyLarge, color = contentColor.copy(alpha = alpha))
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = TripWiseTheme.typography.bodySmall,
                    color = TripWiseTheme.colors.onSurfaceVariant.copy(alpha = alpha),
                )
            }
        }
        if (trailing != null) {
            Text(trailing, style = TripWiseTheme.typography.bodyMedium, color = TripWiseTheme.colors.onSurfaceVariant.copy(alpha = alpha))
        }
        if (onClick != null) Icon(TwIcons.ChevronRight, contentDescription = null, tint = TripWiseTheme.colors.onSurfaceVariant.copy(alpha = alpha))
    }
}

@PreviewLightDark
@Composable
private fun TwListRowPreview() = TripWiseTheme {
    androidx.compose.material3.Surface { TwListRow("Theme", icon = TwIcons.Theme, trailing = "System", onClick = {}) }
}
