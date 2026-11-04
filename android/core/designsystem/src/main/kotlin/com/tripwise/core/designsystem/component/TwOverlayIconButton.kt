package com.tripwise.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tripwise.core.designsystem.theme.TripWiseTheme

/** Round 48dp button with a solid backing so it stays visible on any photo. */
@Composable
fun TwOverlayIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = TripWiseTheme.colors.onSurface,
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = TripWiseTheme.extendedColors.overlayButton,
            contentColor = tint,
        ),
    ) { Icon(icon, contentDescription) }
}
