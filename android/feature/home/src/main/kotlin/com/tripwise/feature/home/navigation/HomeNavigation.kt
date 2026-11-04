package com.tripwise.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.tripwise.feature.home.HomeScreen
import kotlinx.serialization.Serializable

/** Top-level destination: Home dashboard. */
@Serializable
data object HomeRoute

fun NavController.navigateToHome(navOptions: NavOptions? = null) = navigate(HomeRoute, navOptions)

/**
 * Each feature exposes exactly this trio: a @Serializable route, a NavController extension to reach it,
 * and a NavGraphBuilder extension that registers its screen(s). Cross-feature navigation is passed in
 * as lambdas by :app, so features never depend on each other.
 */
fun NavGraphBuilder.homeSection() {
    composable<HomeRoute> { HomeScreen() }
}
