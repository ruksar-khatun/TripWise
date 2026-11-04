package com.tripwise.app.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tripwise.app.ui.FoundationScreen
import kotlinx.serialization.Serializable

/**
 * Type-safe routes. Each feature module will expose its own route + `NavGraphBuilder.xxxScreen()`
 * extension, and this host only wires them together.
 */
@Serializable
data object FoundationRoute

@Composable
fun TripWiseNavHost(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = FoundationRoute,
        modifier = modifier,
    ) {
        composable<FoundationRoute> {
            FoundationScreen(contentPadding = contentPadding)
        }
    }
}
