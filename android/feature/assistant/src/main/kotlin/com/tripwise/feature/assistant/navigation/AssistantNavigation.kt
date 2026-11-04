package com.tripwise.feature.assistant.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.tripwise.feature.assistant.AssistantPlaceholderScreen
import kotlinx.serialization.Serializable

/** Top-level destination for the Assistant tab. */
@Serializable
data object AssistantRoute

fun NavController.navigateToAssistant(navOptions: NavOptions? = null) = navigate(AssistantRoute, navOptions)

fun NavGraphBuilder.assistantSection() {
    composable<AssistantRoute> { AssistantPlaceholderScreen() }
}
