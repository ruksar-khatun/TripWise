package com.tripwise.feature.trips.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.tripwise.feature.trips.TripsPlaceholderScreen
import kotlinx.serialization.Serializable

/** Top-level destination for the My Trips tab. */
@Serializable
data object TripsRoute

fun NavController.navigateToTrips(navOptions: NavOptions? = null) = navigate(TripsRoute, navOptions)

fun NavGraphBuilder.tripsSection() {
    composable<TripsRoute> { TripsPlaceholderScreen() }
}
