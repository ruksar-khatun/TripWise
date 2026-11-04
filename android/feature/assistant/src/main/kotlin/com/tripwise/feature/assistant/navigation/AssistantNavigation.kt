package com.tripwise.feature.assistant.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tripwise.feature.assistant.AssistantScreen
import kotlinx.serialization.Serializable

/** Full-screen assistant (not a tab). Optional context: a trip or a destination. */
@Serializable
data class AssistantRoute(val tripId: String? = null, val destinationId: String? = null)

fun NavController.navigateToAssistant(tripId: String? = null, destinationId: String? = null) =
    navigate(AssistantRoute(tripId, destinationId)) { launchSingleTop = true }

fun NavGraphBuilder.assistantScreen(onBack: () -> Unit) {
    composable<AssistantRoute> { AssistantScreen(onBack) }
}
