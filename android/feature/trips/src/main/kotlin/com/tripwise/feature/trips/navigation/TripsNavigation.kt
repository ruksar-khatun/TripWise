package com.tripwise.feature.trips.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.tripwise.feature.trips.create.CreateTripScreen
import com.tripwise.feature.trips.detail.TripDetailScreen
import com.tripwise.feature.trips.list.TripsScreen
import kotlinx.serialization.Serializable

/** Top-level destination: My Trips. */
@Serializable
data object TripsRoute

@Serializable
data class TripDetailRoute(val tripId: String)

@Serializable
data class CreateTripRoute(val destinationId: String? = null)

fun NavController.navigateToTrips(navOptions: NavOptions? = null) = navigate(TripsRoute, navOptions)

fun NavController.navigateToTripDetail(tripId: String) = navigate(TripDetailRoute(tripId)) { launchSingleTop = true }

fun NavController.navigateToCreateTrip(destinationId: String? = null) = navigate(CreateTripRoute(destinationId)) {
    launchSingleTop = true
}

fun NavGraphBuilder.tripsSection(
    onTripClick: (String) -> Unit,
    onNewTrip: () -> Unit,
    onBack: () -> Unit,
    onPlanWithAi: (tripId: String) -> Unit,
) {
    composable<TripsRoute> { TripsScreen(onTripClick, onNewTrip) }
    composable<TripDetailRoute> { TripDetailScreen(onBack, onPlanWithAi) }
    composable<CreateTripRoute> { CreateTripScreen(onClose = onBack) }
}
