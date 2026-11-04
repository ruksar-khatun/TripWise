package com.tripwise.core.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme

@Composable
fun TwNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = TripWiseTheme.colors.surfaceContainer,
        content = content,
    )
}

@Composable
fun RowScope.TwNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(if (selected) selectedIcon else icon, contentDescription = null) },
        label = { Text(label) },
        modifier = modifier,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = TripWiseTheme.colors.onPrimaryContainer,
            indicatorColor = TripWiseTheme.colors.primaryContainer,
        ),
    )
}

@PreviewLightDark
@Composable
private fun TwNavigationBarPreview() = TripWiseTheme {
    TwNavigationBar {
        TwNavigationBarItem(true, {}, TwIcons.Home, TwIcons.HomeSelected, "Home")
        TwNavigationBarItem(false, {}, TwIcons.Explore, TwIcons.ExploreSelected, "Explore")
        TwNavigationBarItem(false, {}, TwIcons.Trips, TwIcons.TripsSelected, "Trips")
    }
}
