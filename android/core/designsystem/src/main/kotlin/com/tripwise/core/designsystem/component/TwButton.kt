package com.tripwise.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme

private val ButtonPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)

/** Primary filled button. While [loading], shows a spinner and ignores clicks. */
@Composable
fun TwButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading,
        contentPadding = ButtonPadding,
    ) { ButtonContent(text, loading, leadingIcon) }
}

/** Secondary outlined button. */
@Composable
fun TwOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading,
        contentPadding = ButtonPadding,
    ) { ButtonContent(text, loading, leadingIcon) }
}

/** Low-emphasis text button. */
@Composable
fun TwTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(onClick = onClick, modifier = modifier, enabled = enabled) { Text(text) }
}

@Composable
private fun RowScope.ButtonContent(text: String, loading: Boolean, leadingIcon: ImageVector?) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            loading -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = LocalContentColor.current,
            )
            leadingIcon != null -> Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Text(text)
    }
}

@PreviewLightDark
@Composable
private fun TwButtonPreview() = TripWiseTheme {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TwButton("Primary", onClick = {}, leadingIcon = TwIcons.Refresh)
        TwOutlinedButton("Loading", onClick = {}, loading = true)
        TwTextButton("Text", onClick = {})
    }
}
