package com.tripwise.feature.itinerary.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tripwise.feature.itinerary.PlannerScreen
import kotlinx.serialization.Serializable

/** AI itinerary planner. Opened for a destination (new trip) or an existing trip. */
@Serializable data class PlannerRoute(val destinationId: String? = null, val tripId: String? = null)

fun NavController.navigateToPlanner(destinationId: String? = null, tripId: String? = null) =
    navigate(PlannerRoute(destinationId, tripId)) { launchSingleTop = true }

fun NavGraphBuilder.plannerScreen(onBack: () -> Unit, onSaved: (tripId: String) -> Unit) {
    composable<PlannerRoute> { PlannerScreen(onBack, onSaved) }
}
