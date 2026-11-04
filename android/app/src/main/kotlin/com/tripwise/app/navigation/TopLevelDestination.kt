package com.tripwise.app.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.tripwise.app.R
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.feature.assistant.navigation.AssistantRoute
import com.tripwise.feature.explore.navigation.ExploreRoute
import com.tripwise.feature.home.navigation.HomeRoute
import com.tripwise.feature.profile.navigation.ProfileRoute
import com.tripwise.feature.trips.navigation.TripsRoute
import kotlin.reflect.KClass

/** Bottom-bar destinations. Each tab keeps its own back stack. */
enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    @StringRes val labelRes: Int,
) {
    HOME(HomeRoute, HomeRoute::class, TwIcons.Home, TwIcons.HomeSelected, R.string.nav_home),
    EXPLORE(ExploreRoute, ExploreRoute::class, TwIcons.Explore, TwIcons.ExploreSelected, R.string.nav_explore),
    TRIPS(TripsRoute, TripsRoute::class, TwIcons.Trips, TwIcons.TripsSelected, R.string.nav_trips),
    ASSISTANT(AssistantRoute, AssistantRoute::class, TwIcons.Assistant, TwIcons.AssistantSelected, R.string.nav_assistant),
    PROFILE(ProfileRoute, ProfileRoute::class, TwIcons.Profile, TwIcons.ProfileSelected, R.string.nav_profile),
}
