package com.tripwise.feature.destination.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.tripwise.feature.destination.DestinationDetailNavigation
import com.tripwise.feature.destination.DestinationDetailScreen
import com.tripwise.feature.destination.extras.ActivitiesScreen
import com.tripwise.feature.destination.extras.DestinationMapScreen
import kotlinx.serialization.Serializable

@Serializable data class DestinationRoute(val destinationId: String)
@Serializable data class ActivitiesRoute(val destinationId: String)
@Serializable data class DestinationMapRoute(val destinationId: String)

fun NavController.navigateToDestination(destinationId: String) = navigate(DestinationRoute(destinationId)) { launchSingleTop = true }
fun NavController.navigateToActivities(destinationId: String) = navigate(ActivitiesRoute(destinationId)) { launchSingleTop = true }
fun NavController.navigateToDestinationMap(destinationId: String) = navigate(DestinationMapRoute(destinationId)) { launchSingleTop = true }

fun NavGraphBuilder.destinationScreen(navigation: DestinationDetailNavigation) {
    composable<DestinationRoute> { entry ->
        DestinationDetailScreen(entry.toRoute<DestinationRoute>().destinationId, navigation)
    }
    composable<ActivitiesRoute> { entry ->
        val id = entry.toRoute<ActivitiesRoute>().destinationId
        ActivitiesScreen(id, onBack = navigation.onBack, onOpenMap = { navigation.onOpenMap(id) })
    }
    composable<DestinationMapRoute> { entry ->
        DestinationMapScreen(entry.toRoute<DestinationMapRoute>().destinationId, onBack = navigation.onBack)
    }
}
