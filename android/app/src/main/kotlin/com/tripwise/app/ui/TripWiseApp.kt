package com.tripwise.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.app.navigation.TripWiseNavHost
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.ui.state.OfflineBanner

/**
 * App shell. NavigationSuiteScaffold picks a bottom bar (phones) or navigation rail
 * (medium/expanded widths: foldables, tablets) from the window size; hidden on detail screens.
 */
@Composable
fun TripWiseApp(appState: TripWiseAppState, modifier: Modifier = Modifier) {
    val isOffline by appState.isOffline.collectAsStateWithLifecycle()
    val currentDestination = appState.currentDestination
    val layoutType = if (appState.showNavigation) {
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
    } else {
        NavigationSuiteType.None
    }
    val selectedColors = TripWiseTheme.colors
    NavigationSuiteScaffold(
        modifier = modifier,
        layoutType = layoutType,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = TripWiseTheme.colors.surfaceContainerLowest,
            navigationRailContainerColor = TripWiseTheme.colors.surfaceContainerLowest,
        ),
        navigationSuiteItems = {
            appState.topLevelDestinations.forEach { tab ->
                val selected = appState.isSelected(currentDestination, tab)
                item(
                    selected = selected,
                    onClick = { appState.navigateToTopLevel(tab) },
                    icon = { Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = null) },
                    label = { Text(stringResource(tab.labelRes)) },
                    colors = NavigationSuiteDefaults.itemColors(
                        navigationBarItemColors = NavigationBarItemDefaults.colors(
                            indicatorColor = selectedColors.secondaryContainer,
                            selectedIconColor = selectedColors.onSecondaryContainer,
                            selectedTextColor = selectedColors.primary,
                        ),
                        navigationRailItemColors = NavigationRailItemDefaults.colors(
                            indicatorColor = selectedColors.secondaryContainer,
                            selectedIconColor = selectedColors.onSecondaryContainer,
                            selectedTextColor = selectedColors.primary,
                        ),
                    ),
                )
            }
        },
    ) {
        Column(Modifier.fillMaxSize()) {
            if (isOffline) OfflineBanner()
            TripWiseNavHost(appState = appState, modifier = Modifier.weight(1f))
        }
    }
}
