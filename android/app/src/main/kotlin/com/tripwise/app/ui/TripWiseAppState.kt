package com.tripwise.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.tripwise.app.navigation.TopLevelDestination
import com.tripwise.core.common.network.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Composable
fun rememberTripWiseAppState(
    networkMonitor: NetworkMonitor,
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): TripWiseAppState = remember(navController, networkMonitor, coroutineScope) {
    TripWiseAppState(navController, networkMonitor, coroutineScope)
}

/** App-shell UI state: navigation, top-level tab selection and connectivity. */
@Stable
class TripWiseAppState(
    val navController: NavHostController,
    networkMonitor: NetworkMonitor,
    coroutineScope: CoroutineScope,
) {
    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries

    val isOffline: StateFlow<Boolean> = networkMonitor.isOnline
        .map { !it }
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), initialValue = false)

    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination

    /** The bar is shown only on top-level screens; detail screens are full-screen. */
    val showBottomBar: Boolean
        @Composable get() = currentDestination.let { dest ->
            dest == null || topLevelDestinations.any { dest.hasRoute(it.routeClass) }
        }

    fun isSelected(destination: NavDestination?, tab: TopLevelDestination): Boolean =
        destination?.hierarchy?.any { it.hasRoute(tab.routeClass) } == true

    /** Single-top, per-tab back stack saved and restored when switching tabs. */
    fun navigateToTopLevel(tab: TopLevelDestination) {
        navController.navigate(
            tab.route,
            navOptions {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            },
        )
    }
}
