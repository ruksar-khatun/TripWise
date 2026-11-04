package com.tripwise.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tripwise.feature.auth.SignInScreen
import com.tripwise.feature.auth.SignUpScreen
import kotlinx.serialization.Serializable

@Serializable data object SignInRoute
@Serializable data object SignUpRoute

/** Signed-out graph. Signing in flips the session; the app shell then shows the main graph. */
fun NavGraphBuilder.authGraph(navController: NavController, sessionExpired: Boolean) {
    composable<SignInRoute> {
        SignInScreen(sessionExpired, onCreateAccount = { navController.navigate(SignUpRoute) { launchSingleTop = true } })
    }
    composable<SignUpRoute> {
        SignUpScreen(onSignIn = { if (!navController.popBackStack()) navController.navigate(SignInRoute) })
    }
}
