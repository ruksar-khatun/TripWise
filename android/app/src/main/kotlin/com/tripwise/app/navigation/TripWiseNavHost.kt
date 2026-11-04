package com.tripwise.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import com.tripwise.app.BuildConfig
import com.tripwise.app.ui.TripWiseAppState
import com.tripwise.feature.assistant.navigation.assistantScreen
import com.tripwise.feature.assistant.navigation.navigateToAssistant
import com.tripwise.feature.destination.DestinationDetailNavigation
import com.tripwise.feature.destination.DestinationDetailScreen
import com.tripwise.feature.destination.navigation.destinationScreen
import com.tripwise.feature.destination.navigation.navigateToDestination
import com.tripwise.feature.explore.navigation.ExploreRoute
import com.tripwise.feature.explore.navigation.exploreSection
import com.tripwise.feature.home.HomeNavigation
import com.tripwise.feature.home.navigation.HomeRoute
import com.tripwise.feature.home.navigation.homeSection
import com.tripwise.feature.profile.navigation.profileSection
import com.tripwise.feature.saved.navigation.savedSection
import com.tripwise.feature.trips.navigation.navigateToCreateTrip
import com.tripwise.feature.trips.navigation.navigateToTripDetail
import com.tripwise.feature.trips.navigation.tripsSection
import com.tripwise.core.ui.layout.DetailPane

private const val TAB_FADE_MS = 200
private const val PUSH_MS = 300

/**
 * The only place that knows every feature; cross-feature navigation is wired here.
 * Motion: tab ↔ tab = quick fade-through; push/pop of detail screens = horizontal slide + fade.
 * An auth graph (session gate → sign-in) wraps this when authentication is approved.
 */
@Composable
fun TripWiseNavHost(appState: TripWiseAppState, modifier: Modifier = Modifier) {
    val nav = appState.navController
    val back: () -> Unit = { nav.popBackStack() }

    val destinationNavigation = DestinationDetailNavigation(
        onBack = back,
        onPlanTrip = { nav.navigateToCreateTrip(it) },
        onPlanWithAi = { nav.navigateToAssistant(destinationId = it) },
    )
    // Tablet two-pane: detail rendered next to the list, "back" closes the pane.
    val detailPane: DetailPane = { id, onClose ->
        DestinationDetailScreen(id, destinationNavigation.copy(onBack = onClose), isPane = true)
    }

    fun isTab(entry: NavBackStackEntry) = appState.isTopLevel(entry.destination)
    fun AnimatedContentTransitionScope<NavBackStackEntry>.bothTabs() = isTab(initialState) && isTab(targetState)

    NavHost(
        navController = nav,
        startDestination = HomeRoute,
        modifier = modifier,
        enterTransition = {
            if (bothTabs()) fadeIn(tween(TAB_FADE_MS)) else slideIn(AnimatedContentTransitionScope.SlideDirection.Start)
        },
        exitTransition = {
            if (bothTabs()) fadeOut(tween(TAB_FADE_MS)) else slideOut(AnimatedContentTransitionScope.SlideDirection.Start)
        },
        popEnterTransition = {
            if (bothTabs()) fadeIn(tween(TAB_FADE_MS)) else slideIn(AnimatedContentTransitionScope.SlideDirection.End)
        },
        popExitTransition = {
            if (bothTabs()) fadeOut(tween(TAB_FADE_MS)) else slideOut(AnimatedContentTransitionScope.SlideDirection.End)
        },
    ) {
        homeSection(
            HomeNavigation(
                onDestinationClick = { nav.navigateToDestination(it) },
                onTripClick = { nav.navigateToTripDetail(it) },
                onSearchClick = { appState.navigateToTopLevel(TopLevelDestination.EXPLORE, ExploreRoute(focusSearch = true), restore = false) },
                onInterestClick = { appState.navigateToTopLevel(TopLevelDestination.EXPLORE, ExploreRoute(interest = it.name), restore = false) },
                onSeeAllDestinations = { appState.navigateToTopLevel(TopLevelDestination.EXPLORE) },
                onSeeAllSaved = { appState.navigateToTopLevel(TopLevelDestination.SAVED) },
                onPlanTrip = { nav.navigateToCreateTrip() },
                onAssistantClick = { nav.navigateToAssistant() },
                onProfileClick = { appState.navigateToTopLevel(TopLevelDestination.PROFILE) },
            ),
        )
        exploreSection(onDestinationClick = { nav.navigateToDestination(it) }, detailPane = detailPane)
        savedSection(
            onDestinationClick = { nav.navigateToDestination(it) },
            onExploreClick = { appState.navigateToTopLevel(TopLevelDestination.EXPLORE) },
            detailPane = detailPane,
        )
        tripsSection(
            onTripClick = { nav.navigateToTripDetail(it) },
            onNewTrip = { nav.navigateToCreateTrip() },
            onBack = back,
            onPlanWithAi = { nav.navigateToAssistant(tripId = it) },
        )
        profileSection(appVersion = BuildConfig.VERSION_NAME)
        destinationScreen(destinationNavigation)
        assistantScreen(onBack = back)
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.slideIn(
    direction: AnimatedContentTransitionScope.SlideDirection,
): EnterTransition = slideIntoContainer(direction, tween(PUSH_MS)) + fadeIn(tween(PUSH_MS))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.slideOut(
    direction: AnimatedContentTransitionScope.SlideDirection,
): ExitTransition = slideOutOfContainer(direction, tween(PUSH_MS), targetOffset = { it / 4 }) + fadeOut(tween(PUSH_MS))
