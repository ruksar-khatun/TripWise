package com.example.tripwise.data

import androidx.annotation.DrawableRes

enum class TripStatus {
    UPCOMING, ONGOING, COMPLETED
}

data class Attraction(
    val name: String,
    val description: String,
    @DrawableRes val imageRes: Int
)

data class Destination(
    val id: String,
    val name: String,
    val location: String,
    val country: String,
    val description: String,
    @DrawableRes val imageRes: Int,
    val rating: Double,
    val reviews: Int,
    val estimatedCost: Double,
    val category: String, // e.g. Culture, Nature, Beach, Heritage, Luxury
    val attractions: List<Attraction> = emptyList(),
    var isSaved: Boolean = false,
    var localFood: String = "",
    var accommodation: String = "",
    var transport: String = "",
    var about: String = "",
    var whyVisit: String = "",
    var thingsToDo: List<String> = emptyList()
)

data class TravelActivity(
    val id: String,
    val dayIndex: Int, // 0 = Day 1, 1 = Day 2, etc.
    val title: String,
    val location: String,
    val description: String,
    val time: String,
    val duration: String,
    val cost: Double,
    @DrawableRes val imageRes: Int? = null,
    var isCompleted: Boolean = false
)

data class BudgetBreakdown(
    var transport: Double = 0.0,
    var accommodation: Double = 0.0,
    var food: Double = 0.0,
    var activities: Double = 0.0,
    var miscellaneous: Double = 0.0
) {
    val total: Double get() = transport + accommodation + food + activities + miscellaneous
}

data class Trip(
    val id: String,
    val tripName: String,
    val destinationName: String,
    val startDateString: String,
    val endDateString: String,
    val travellersCount: Int,
    val totalBudgetLimit: Double,
    val budgetBreakdown: BudgetBreakdown = BudgetBreakdown(),
    val interests: List<String> = emptyList(),
    val travelStyle: String = "Balanced",
    val status: TripStatus = TripStatus.UPCOMING,
    val activities: MutableList<TravelActivity> = mutableListOf()
)

data class UserProfile(
    var name: String,
    var email: String,
    var phone: String = "+91 98765 43210",
    @DrawableRes var avatarRes: Int,
    var avatarUri: String? = null,
    var totalTripsPlanned: Int = 5,
    var countriesVisited: Int = 8,
    var savedCount: Int = 3,
    var interests: List<String> = listOf("Nature", "Culture", "Photography", "Food"),
    var travelStyle: String = "Adventure",
    var companion: String = "Friends",
    var budgetRange: String = "Mid-range"
)
