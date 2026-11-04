package com.tripwise.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.tripwise.app.ui.TripWiseAppState
import com.tripwise.feature.assistant.navigation.assistantSection
import com.tripwise.feature.explore.navigation.exploreSection
import com.tripwise.feature.home.navigation.HomeRoute
import com.tripwise.feature.home.navigation.homeSection
import com.tripwise.feature.profile.navigation.profileSection
import com.tripwise.feature.trips.navigation.tripsSection

/**
 * The only place that knows about every feature. Cross-feature navigation
 * (e.g. Home → Destination details) is wired here by passing lambdas into the sections.
 * An auth graph (session gate → sign-in) will wrap this when authentication is approved.
 */
@Composable
fun TripWiseNavHost(appState: TripWiseAppState, modifier: Modifier = Modifier) {
    NavHost(
        navController = appState.navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        homeSection()
        exploreSection()
        tripsSection()
        assistantSection()
        profileSection()
    }
}
