package com.tripwise.feature.saved.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.tripwise.core.ui.layout.DetailPane
import com.tripwise.feature.saved.SavedScreen
import kotlinx.serialization.Serializable

/** Top-level destination: Saved destinations. */
@Serializable
data object SavedRoute

fun NavController.navigateToSaved(navOptions: NavOptions? = null) = navigate(SavedRoute, navOptions)

fun NavGraphBuilder.savedSection(
    onDestinationClick: (String) -> Unit,
    onExploreClick: () -> Unit,
    detailPane: DetailPane,
) {
    composable<SavedRoute> { SavedScreen(onDestinationClick, onExploreClick, detailPane) }
}
