package com.tripwise.feature.assistant.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tripwise.core.model.PlaceKind
import com.tripwise.feature.assistant.AssistantScreen
import com.tripwise.feature.assistant.history.ChatHistoryScreen
import kotlinx.serialization.Serializable

/**
 * TripMate AI chat (full screen, not a tab).
 * @param conversationId resume a saved chat; otherwise a new chat about the optional trip/destination.
 * @param prompt sent as the first message. @param starter PlaceKind name: open with stay/restaurant cards.
 */
@Serializable
data class AssistantRoute(
    val tripId: String? = null,
    val destinationId: String? = null,
    val conversationId: String? = null,
    val prompt: String? = null,
    val starter: String? = null,
)

@Serializable data object ChatHistoryRoute

fun NavController.navigateToAssistant(
    tripId: String? = null,
    destinationId: String? = null,
    prompt: String? = null,
    starter: PlaceKind? = null,
) = navigate(AssistantRoute(tripId, destinationId, prompt = prompt?.take(2_000), starter = starter?.name))

fun NavController.navigateToChatHistory() = navigate(ChatHistoryRoute) { launchSingleTop = true }

/** Navigation out of TripMate AI; wired by :app. */
data class AssistantNavigation(
    val onBack: () -> Unit,
    val onOpenHistory: () -> Unit,
    /** Structured, saveable itinerary for a destination or trip. */
    val onOpenPlanner: (destinationId: String?, tripId: String?) -> Unit,
)

fun NavGraphBuilder.assistantScreens(navigation: AssistantNavigation, nav: NavController) {
    composable<AssistantRoute> { AssistantScreen(navigation) }
    composable<ChatHistoryRoute> {
        ChatHistoryScreen(
            onBack = navigation.onBack,
            onOpen = { id ->
                // Replace the history screen so Back returns to where the user came from.
                nav.navigate(AssistantRoute(conversationId = id)) { popUpTo<ChatHistoryRoute> { inclusive = true } }
            },
            onNewChat = { nav.navigate(AssistantRoute()) { popUpTo<ChatHistoryRoute> { inclusive = true } } },
        )
    }
}
