package com.tripwise.feature.destination.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.tripwise.feature.destination.DestinationDetailNavigation
import com.tripwise.feature.destination.DestinationDetailScreen
import com.tripwise.feature.destination.extras.ActivitiesScreen
import kotlinx.serialization.Serializable

@Serializable data class DestinationRoute(val destinationId: String)
@Serializable data class ActivitiesRoute(val destinationId: String)

fun NavController.navigateToDestination(destinationId: String) = navigate(DestinationRoute(destinationId)) { launchSingleTop = true }
fun NavController.navigateToActivities(destinationId: String) = navigate(ActivitiesRoute(destinationId)) { launchSingleTop = true }

fun NavGraphBuilder.destinationScreen(navigation: DestinationDetailNavigation) {
    composable<DestinationRoute> { entry ->
        DestinationDetailScreen(entry.toRoute<DestinationRoute>().destinationId, navigation)
    }
    composable<ActivitiesRoute> { entry ->
        ActivitiesScreen(entry.toRoute<ActivitiesRoute>().destinationId, onBack = navigation.onBack)
    }
}
