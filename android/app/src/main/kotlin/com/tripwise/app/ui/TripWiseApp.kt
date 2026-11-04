package com.tripwise.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.app.navigation.TripWiseNavHost
import com.tripwise.core.designsystem.component.TwNavigationBar
import com.tripwise.core.designsystem.component.TwNavigationBarItem
import com.tripwise.core.ui.state.OfflineBanner

/**
 * App shell: bottom navigation, offline banner, and the NavHost.
 * Feature screens own their own Scaffold/top bar, so the shell consumes no top insets.
 */
@Composable
fun TripWiseApp(appState: TripWiseAppState, modifier: Modifier = Modifier) {
    val isOffline by appState.isOffline.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentDestination = appState.currentDestination

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (appState.showBottomBar) {
                TwNavigationBar {
                    appState.topLevelDestinations.forEach { tab ->
                        TwNavigationBarItem(
                            selected = appState.isSelected(currentDestination, tab),
                            onClick = { appState.navigateToTopLevel(tab) },
                            icon = tab.icon,
                            selectedIcon = tab.selectedIcon,
                            label = stringResource(tab.labelRes),
                        )
                    }
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (isOffline) OfflineBanner()
            TripWiseNavHost(appState = appState, modifier = Modifier.weight(1f))
        }
    }
}
