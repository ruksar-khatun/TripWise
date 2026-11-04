package com.tripwise.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.tripwise.core.designsystem.theme.TripWiseTheme

/** Selectable chip for filters and tags. */
@Composable
fun TwFilterChip(
    label: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    FilterChip(
        selected = selected,
        onClick = { onSelectedChange(!selected) },
        label = { Text(label) },
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, Modifier.size(18.dp)) } },
        shape = TripWiseTheme.shapes.chip,
    )
}

@PreviewLightDark
@Composable
private fun TwFilterChipPreview() = TripWiseTheme {
    TwFilterChip(label = "Beaches", selected = true, onSelectedChange = {})
}
