package com.example.tripwise.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tripwise.data.AppState
import com.example.tripwise.ui.components.TripWiseBottomBar
import com.example.tripwise.ui.screens.auth.*
import com.example.tripwise.ui.screens.home.*
import com.example.tripwise.ui.screens.profile.*
import com.example.tripwise.ui.screens.trip.*

@Composable
fun TripWiseNavGraph(
    appState: AppState,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Splash.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Explore.route,
        Screen.SavedPlaces.route,
        Screen.MyTrips.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                TripWiseBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onGetStarted = { navController.navigate(Screen.Onboarding.route) }
                )
            }
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onSignUp = { navController.navigate(Screen.SignUp.route) },
                    onLogin = { navController.navigate(Screen.Login.route) }
                )
            }
            composable(Screen.SignUp.route) {
                SignUpScreen(
                    appState = appState,
                    onSignUpSuccess = { navController.navigate(Screen.Preferences.route) },
                    onLoginClick = { navController.navigate(Screen.Login.route) }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    appState = appState,
                    onLoginSuccess = { navController.navigate(Screen.Home.route) },
                    onSignUpClick = { navController.navigate(Screen.SignUp.route) }
                )
            }
            composable(Screen.Preferences.route) {
                PreferencesScreen(
                    appState = appState,
                    onComplete = { navController.navigate(Screen.Home.route) }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    appState = appState,
                    onDestinationClick = { navController.navigate(Screen.DestinationDetails.createRoute(it)) },
                    onExploreClick = { navController.navigate(Screen.Explore.route) },
                    onProfileClick = { navController.navigate(Screen.Profile.route) },
                    onCreateTripClick = { navController.navigate(Screen.CreateTrip.route) },
                    onSavedPlacesClick = { navController.navigate(Screen.SavedPlaces.route) },
                    onCurrentTripClick = { navController.navigate(Screen.CurrentTrip.route) }
                )
            }
            composable(Screen.Explore.route) {
                ExploreScreen(
                    appState = appState,
                    onDestinationClick = { navController.navigate(Screen.DestinationDetails.createRoute(it)) },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.DestinationDetails.route,
                arguments = listOf(navArgument("destinationId") {})
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("destinationId") ?: ""
                DestinationDetailsScreen(
                    destinationId = id,
                    appState = appState,
                    onBackClick = { navController.popBackStack() },
                    onAddToTripClick = { navController.navigate(Screen.CreateTrip.route) }
                )
            }
            composable(Screen.SavedPlaces.route) {
                SavedPlacesScreen(
                    appState = appState,
                    onDestinationClick = { navController.navigate(Screen.DestinationDetails.createRoute(it)) },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.CreateTrip.route) {
                CreateTripScreen(
                    appState = appState,
                    onBackClick = { navController.popBackStack() },
                    onTripCreated = { tripId ->
                        navController.navigate(Screen.Itinerary.createRoute(tripId)) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }
            composable(
                route = Screen.Itinerary.route,
                arguments = listOf(navArgument("tripId") {})
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("tripId") ?: ""
                ItineraryScreen(
                    tripId = id,
                    appState = appState,
                    onBackClick = { navController.popBackStack() },
                    onActivityClick = { activityId ->
                        navController.navigate(Screen.ActivityDetails.createRoute(activityId))
                    }
                )
            }
            composable(
                route = Screen.ActivityDetails.route,
                arguments = listOf(navArgument("activityId") {})
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("activityId") ?: ""
                ActivityDetailsScreen(
                    activityId = id,
                    appState = appState,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.TripBudget.route,
                arguments = listOf(navArgument("tripId") {})
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("tripId") ?: ""
                TripBudgetScreen(
                    tripId = id,
                    appState = appState,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.TripSummary.route,
                arguments = listOf(navArgument("tripId") {})
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("tripId") ?: ""
                TripSummaryScreen(
                    tripId = id,
                    appState = appState,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.MyTrips.route) {
                MyTripsScreen(
                    appState = appState,
                    onTripClick = { tripId ->
                        navController.navigate(Screen.TripOverview.createRoute(tripId))
                    }
                )
            }
            composable(
                route = Screen.TripOverview.route,
                arguments = listOf(navArgument("tripId") {})
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("tripId") ?: ""
                TripOverviewScreen(
                    tripId = id,
                    appState = appState,
                    onBackClick = { navController.popBackStack() },
                    onItineraryClick = { navController.navigate(Screen.Itinerary.createRoute(id)) },
                    onBudgetClick = { navController.navigate(Screen.TripBudget.createRoute(id)) }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    appState = appState,
                    onEditPreferencesClick = { navController.navigate(Screen.EditPreferences.route) },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onLogoutClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.EditPreferences.route) {
                EditPreferencesScreen(
                    appState = appState,
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    appState = appState,
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.CurrentTrip.route) {
                CurrentTripScreen(
                    appState = appState,
                    onActivityClick = { activityId ->
                        navController.navigate(Screen.ActivityDetails.createRoute(activityId))
                    }
                )
            }
        }
    }
}
