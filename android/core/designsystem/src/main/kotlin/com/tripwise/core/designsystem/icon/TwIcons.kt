package com.tripwise.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Hiking
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.LocalActivity
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Museum
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

/** Single place that maps semantic icon names to Material symbols. Screens never import Icons.* directly. */
object TwIcons {
    // Navigation
    val Back: ImageVector = Icons.AutoMirrored.Outlined.ArrowBack
    val Home: ImageVector = Icons.Outlined.Home
    val HomeSelected: ImageVector = Icons.Filled.Home
    val Explore: ImageVector = Icons.Outlined.TravelExplore
    val ExploreSelected: ImageVector = Icons.Filled.TravelExplore
    val Saved: ImageVector = Icons.Outlined.FavoriteBorder
    val SavedSelected: ImageVector = Icons.Filled.Favorite
    val Trips: ImageVector = Icons.Outlined.Luggage
    val TripsSelected: ImageVector = Icons.Filled.Luggage
    val Profile: ImageVector = Icons.Outlined.AccountCircle
    val ProfileSelected: ImageVector = Icons.Filled.AccountCircle
    val ChevronRight: ImageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight

    // Actions
    val Assistant: ImageVector = Icons.Outlined.AutoAwesome
    val Search: ImageVector = Icons.Outlined.Search
    val Clear: ImageVector = Icons.Outlined.Close
    val Add: ImageVector = Icons.Outlined.Add
    val Remove: ImageVector = Icons.Outlined.Remove
    val Send: ImageVector = Icons.AutoMirrored.Outlined.Send
    val Share: ImageVector = Icons.Outlined.Share
    val Sort: ImageVector = Icons.AutoMirrored.Outlined.Sort
    val Check: ImageVector = Icons.Outlined.Check
    val Refresh: ImageVector = Icons.Outlined.Refresh
    val Delete: ImageVector = Icons.Outlined.Delete
    val SignOut: ImageVector = Icons.AutoMirrored.Outlined.Logout
    val Favorite: ImageVector = Icons.Filled.Favorite
    val FavoriteOutline: ImageVector = Icons.Outlined.FavoriteBorder

    // Information
    val Star: ImageVector = Icons.Filled.Star
    val Calendar: ImageVector = Icons.Outlined.CalendarMonth
    val Travellers: ImageVector = Icons.Outlined.Group
    val Wallet: ImageVector = Icons.Outlined.AccountBalanceWallet
    val Sun: ImageVector = Icons.Outlined.WbSunny
    val Currency: ImageVector = Icons.Outlined.CurrencyExchange
    val Language: ImageVector = Icons.Outlined.Translate
    val Map: ImageVector = Icons.Outlined.Map
    val Place: ImageVector = Icons.Outlined.Place
    val Time: ImageVector = Icons.Outlined.Schedule
    val Notes: ImageVector = Icons.AutoMirrored.Outlined.Notes
    val Theme: ImageVector = Icons.Outlined.Palette
    val Info: ImageVector = Icons.Outlined.Info
    val Email: ImageVector = Icons.Outlined.Email
    val Lock: ImageVector = Icons.Outlined.Lock
    val Visibility: ImageVector = Icons.Outlined.Visibility
    val VisibilityOff: ImageVector = Icons.Outlined.VisibilityOff

    // Interests / categories
    val Beach: ImageVector = Icons.Outlined.BeachAccess
    val Culture: ImageVector = Icons.Outlined.Museum
    val Food: ImageVector = Icons.Outlined.Restaurant
    val Nature: ImageVector = Icons.Outlined.Forest
    val Mountains: ImageVector = Icons.Outlined.Landscape
    val City: ImageVector = Icons.Outlined.LocationCity
    val Adventure: ImageVector = Icons.Outlined.Hiking
    val Lodging: ImageVector = Icons.Outlined.Hotel
    val Transport: ImageVector = Icons.Outlined.DirectionsBus
    val Shopping: ImageVector = Icons.Outlined.ShoppingBag
    val Activity: ImageVector = Icons.Outlined.LocalActivity

    // States
    val Offline: ImageVector = Icons.Outlined.CloudOff
    val Error: ImageVector = Icons.Outlined.ErrorOutline
    val Empty: ImageVector = Icons.Outlined.Inbox
}
