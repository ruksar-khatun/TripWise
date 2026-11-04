package com.tripwise.feature.explore.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.tripwise.core.ui.layout.DetailPane
import com.tripwise.feature.explore.ExploreScreen
import kotlinx.serialization.Serializable

/**
 * Top-level destination: Explore.
 * @param interest optional TravelInterest name to pre-select (read by ExploreViewModel via SavedStateHandle).
 * @param focusSearch open with the keyboard on the search field (from Home's search launcher).
 */
@Serializable
data class ExploreRoute(val interest: String? = null, val focusSearch: Boolean = false)

fun NavController.navigateToExplore(route: ExploreRoute = ExploreRoute(), navOptions: NavOptions? = null) =
    navigate(route, navOptions)

fun NavGraphBuilder.exploreSection(onDestinationClick: (String) -> Unit, detailPane: DetailPane) {
    composable<ExploreRoute> { entry ->
        ExploreScreen(entry.toRoute<ExploreRoute>().focusSearch, onDestinationClick, detailPane)
    }
}
