package com.example.tripwise.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object SignUp : Screen("signup")
    object Login : Screen("login")
    object Preferences : Screen("preferences")
    object Home : Screen("home")
    object Explore : Screen("explore")
    object DestinationDetails : Screen("destination_details/{destinationId}") {
        fun createRoute(destinationId: String) = "destination_details/$destinationId"
    }
    object SavedPlaces : Screen("saved_places")
    object CreateTrip : Screen("create_trip")
    object Itinerary : Screen("itinerary/{tripId}") {
        fun createRoute(tripId: String) = "itinerary/$tripId"
    }
    object ActivityDetails : Screen("activity_details/{activityId}") {
        fun createRoute(activityId: String) = "activity_details/$activityId"
    }
    object TripBudget : Screen("trip_budget/{tripId}") {
        fun createRoute(tripId: String) = "trip_budget/$tripId"
    }
    object TripSummary : Screen("trip_summary/{tripId}") {
        fun createRoute(tripId: String) = "trip_summary/$tripId"
    }
    object MyTrips : Screen("my_trips")
    object TripOverview : Screen("trip_overview/{tripId}") {
        fun createRoute(tripId: String) = "trip_overview/$tripId"
    }
    object Profile : Screen("profile")
    object EditPreferences : Screen("edit_preferences")
    object Settings : Screen("settings")
    object CurrentTrip : Screen("current_trip")
}
