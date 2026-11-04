package com.tripwise.core.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme

/**
 * Top app bar. Shows a back arrow when [onNavigateBack] is set.
 * [centered] is used for top-level tab screens; detail screens use the start-aligned variant.
 * Scroll behaviour is intentionally not exposed: its type is still experimental in Material 3, and
 * exposing it would force every caller to opt in.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    centered: Boolean = false,
    onNavigateBack: (() -> Unit)? = null,
    navigateBackContentDescription: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val titleContent: @Composable () -> Unit = {
        Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
    val navIcon: @Composable () -> Unit = {
        if (onNavigateBack != null) {
            IconButton(onClick = onNavigateBack) {
                Icon(TwIcons.Back, contentDescription = navigateBackContentDescription)
            }
        }
    }
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = TripWiseTheme.colors.surface,
        scrolledContainerColor = TripWiseTheme.colors.surfaceContainer,
    )
    if (centered) {
        CenterAlignedTopAppBar(
            title = titleContent,
            modifier = modifier,
            navigationIcon = navIcon,
            actions = actions,
            colors = colors,
        )
    } else {
        TopAppBar(
            title = titleContent,
            modifier = modifier,
            navigationIcon = navIcon,
            actions = actions,
            colors = colors,
        )
    }
}

@PreviewLightDark
@Composable
private fun TwTopAppBarPreview() = TripWiseTheme {
    TwTopAppBar(title = "Trip details", onNavigateBack = {})
}
