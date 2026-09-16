package com.example.tripwise.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.tripwise.R

class AppState(context: Context) {
    private val prefManager = PreferenceManager(context)

    // Current logged in user profile
    var userProfile by mutableStateOf(
        UserProfile(
            name = prefManager.userName,
            email = prefManager.userEmail,
            phone = prefManager.userPhone,
            avatarRes = prefManager.userAvatarRes,
            avatarUri = prefManager.userAvatarUri,
            totalTripsPlanned = 5,
            countriesVisited = 8,
            savedCount = 4,
            interests = listOf("Nature", "Culture", "Photography", "Food"),
            travelStyle = "Adventure",
            companion = "Friends",
            budgetRange = "Mid-range"
        )
    )

    // Settings state
    var notificationsEnabled by mutableStateOf(true)
    var themeMode by mutableStateOf(prefManager.themeMode) // "Light", "Dark", "System"
    var selectedCurrency by mutableStateOf(prefManager.selectedCurrency)
        
    fun changeTheme(newTheme: String) {
        themeMode = newTheme
        prefManager.themeMode = newTheme
    }

    fun changeCurrency(newCurrency: String) {
        selectedCurrency = newCurrency
        prefManager.selectedCurrency = newCurrency
    }

    var selectedLanguage by mutableStateOf("English")
    var currentExchangeRate by mutableStateOf(1.0)

    val currencySymbol: String
        get() = when {
            selectedCurrency.contains("₹") -> "₹"
            selectedCurrency.contains("€") -> "€"
            selectedCurrency.contains("£") -> "£"
            selectedCurrency.contains("¥") -> "¥"
            selectedCurrency.contains("₩") -> "₩"
            selectedCurrency.contains("S$") -> "S$"
            selectedCurrency.contains("د.إ") -> "د.إ"
            selectedCurrency.contains("฿") -> "฿"
            selectedCurrency.contains("Rp") -> "Rp"
            selectedCurrency.contains("RM") -> "RM"
            selectedCurrency.contains("₱") -> "₱"
            selectedCurrency.contains("₫") -> "₫"
            else -> "$"
        }

    fun getCurrencyCode(): String {
        return try {
            selectedCurrency.substringAfter("(").substringBefore(" ")
        } catch (e: Exception) {
            "USD"
        }
    }

    // Destinations collection initialized from factory
    val destinations = mutableStateListOf(*DestinationsFactory.createDestinations().toTypedArray())

    // Trips list
    val trips = mutableStateListOf(
        Trip(
            id = "t101",
            tripName = "Kerala Munnar Retreat",
            destinationName = "Munnar, Kerala",
            startDateString = "2026-10-10",
            endDateString = "2026-10-15",
            travellersCount = 2,
            totalBudgetLimit = 25000.0,
            budgetBreakdown = BudgetBreakdown(
                transport = 6000.0,
                accommodation = 10000.0,
                food = 5000.0,
                activities = 3000.0,
                miscellaneous = 1000.0
            ),
            interests = listOf("Nature", "Photography"),
            travelStyle = "Scenic Explorer",
            status = TripStatus.UPCOMING,
            activities = mutableListOf(
                TravelActivity(
                    id = "act1",
                    dayIndex = 0,
                    title = "Tea Garden Morning Walk",
                    location = "Munnar Hills",
                    description = "Guided walk through misty emerald green tea plantations.",
                    time = "08:00",
                    duration = "2.5 hrs",
                    cost = 500.0,
                    imageRes = R.drawable.munnar,
                    isCompleted = false
                )
            )
        )
    )

    var selectedDestinationId by mutableStateOf("munnar_india")
    var selectedTripId by mutableStateOf("t101")
    var selectedActivityId by mutableStateOf("act1")

    val selectedDestination: Destination
        get() = destinations.firstOrNull { it.id == selectedDestinationId } ?: destinations.first()

    val selectedTrip: Trip
        get() = trips.firstOrNull { it.id == selectedTripId } ?: trips.first()

    val selectedActivity: TravelActivity?
        get() = selectedTrip.activities.firstOrNull { it.id == selectedActivityId }

    fun toggleSaveDestination(id: String) {
        val index = destinations.indexOfFirst { it.id == id }
        if (index != -1) {
            val d = destinations[index]
            destinations[index] = d.copy(isSaved = !d.isSaved)
            userProfile.savedCount = destinations.count { it.isSaved }
        }
    }

    fun addTrip(trip: Trip) {
        trips.add(0, trip)
        selectedTripId = trip.id
        userProfile.totalTripsPlanned += 1
    }

    fun addActivityToSelectedTrip(activity: TravelActivity) {
        val tripIndex = trips.indexOfFirst { it.id == selectedTripId }
        if (tripIndex != -1) {
            val currentTrip = trips[tripIndex]
            val updatedActivities = currentTrip.activities.toMutableList()
            updatedActivities.add(activity)
            val updatedTrip = currentTrip.copy(activities = updatedActivities)
            updatedTrip.budgetBreakdown.activities += activity.cost
            trips[tripIndex] = updatedTrip
        }
    }

    fun deleteActivityFromSelectedTrip(activityId: String) {
        val tripIndex = trips.indexOfFirst { it.id == selectedTripId }
        if (tripIndex != -1) {
            val currentTrip = trips[tripIndex]
            val activityToRemove = currentTrip.activities.firstOrNull { it.id == activityId }
            val updatedActivities = currentTrip.activities.filter { it.id != activityId }.toMutableList()
            val updatedTrip = currentTrip.copy(activities = updatedActivities)
            if (activityToRemove != null) {
                updatedTrip.budgetBreakdown.activities = (updatedTrip.budgetBreakdown.activities - activityToRemove.cost).coerceAtLeast(0.0)
            }
            trips[tripIndex] = updatedTrip
        }
    }

    fun toggleActivityCompleted(activityId: String) {
        val tripIndex = trips.indexOfFirst { it.id == selectedTripId }
        if (tripIndex != -1) {
            val currentTrip = trips[tripIndex]
            val updatedActivities = currentTrip.activities.map { act ->
                if (act.id == activityId) act.copy(isCompleted = !act.isCompleted) else act
            }.toMutableList()
            trips[tripIndex] = currentTrip.copy(activities = updatedActivities)
        }
    }

    fun updateUserPreferences(interests: List<String>, style: String, companion: String, budgetRange: String) {
        userProfile.interests = interests
        userProfile.travelStyle = style
        userProfile.companion = companion
        userProfile.budgetRange = budgetRange
    }

    fun updateFullProfile(name: String, email: String, phone: String, travelStyle: String, companion: String, budgetRange: String) {
        userProfile.name = name
        userProfile.email = email
        userProfile.phone = phone
        userProfile.travelStyle = travelStyle
        userProfile.companion = companion
        userProfile.budgetRange = budgetRange
        
        prefManager.userName = name
        prefManager.userEmail = email
        prefManager.userPhone = phone
    }

    fun updateProfileAvatar(avatarRes: Int) {
        userProfile.avatarRes = avatarRes
        userProfile.avatarUri = null
        prefManager.userAvatarRes = avatarRes
        prefManager.userAvatarUri = null
    }

    fun updateProfileAvatarUri(uriString: String) {
        userProfile.avatarUri = uriString
        prefManager.userAvatarUri = uriString
    }
}
