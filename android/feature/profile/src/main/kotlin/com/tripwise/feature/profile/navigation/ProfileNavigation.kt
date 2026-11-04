package com.tripwise.feature.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.tripwise.feature.profile.ProfilePlaceholderScreen
import kotlinx.serialization.Serializable

/** Top-level destination for the Profile tab. */
@Serializable
data object ProfileRoute

fun NavController.navigateToProfile(navOptions: NavOptions? = null) = navigate(ProfileRoute, navOptions)

fun NavGraphBuilder.profileSection() {
    composable<ProfileRoute> { ProfilePlaceholderScreen() }
}
