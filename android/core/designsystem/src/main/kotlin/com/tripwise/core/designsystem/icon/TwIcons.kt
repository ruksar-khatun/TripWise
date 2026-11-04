package com.tripwise.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.ui.graphics.vector.ImageVector

/** Single place that maps semantic icon names to Material symbols. Screens never import Icons.* directly. */
object TwIcons {
    val Back: ImageVector = Icons.AutoMirrored.Outlined.ArrowBack
    val Home: ImageVector = Icons.Outlined.Home
    val HomeSelected: ImageVector = Icons.Filled.Home
    val Explore: ImageVector = Icons.Outlined.TravelExplore
    val ExploreSelected: ImageVector = Icons.Filled.TravelExplore
    val Trips: ImageVector = Icons.Outlined.Luggage
    val TripsSelected: ImageVector = Icons.Filled.Luggage
    val Assistant: ImageVector = Icons.Outlined.AutoAwesome
    val AssistantSelected: ImageVector = Icons.Filled.AutoAwesome
    val Profile: ImageVector = Icons.Outlined.AccountCircle
    val ProfileSelected: ImageVector = Icons.Filled.AccountCircle
    val Offline: ImageVector = Icons.Outlined.CloudOff
    val Error: ImageVector = Icons.Outlined.ErrorOutline
    val Empty: ImageVector = Icons.Outlined.Inbox
    val Refresh: ImageVector = Icons.Outlined.Refresh
}
