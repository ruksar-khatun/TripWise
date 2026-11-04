package com.tripwise.feature.explore.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.tripwise.feature.explore.ExplorePlaceholderScreen
import kotlinx.serialization.Serializable

/** Top-level destination for the Explore tab. */
@Serializable
data object ExploreRoute

fun NavController.navigateToExplore(navOptions: NavOptions? = null) = navigate(ExploreRoute, navOptions)

fun NavGraphBuilder.exploreSection() {
    composable<ExploreRoute> { ExplorePlaceholderScreen() }
}
