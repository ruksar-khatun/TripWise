package com.tripwise.feature.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.tripwise.feature.profile.ProfileScreen
import kotlinx.serialization.Serializable

/** Top-level destination: Profile. */
@Serializable
data object ProfileRoute

fun NavController.navigateToProfile(navOptions: NavOptions? = null) = navigate(ProfileRoute, navOptions)

fun NavGraphBuilder.profileSection(appVersion: String) {
    composable<ProfileRoute> { ProfileScreen(appVersion) }
}
