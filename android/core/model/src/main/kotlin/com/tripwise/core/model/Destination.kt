package com.tripwise.core.model

enum class TravelInterest { BEACHES, CULTURE, FOOD, NATURE, MOUNTAINS, CITY, ADVENTURE }

data class Destination(
    val id: String,
    val name: String,
    val country: String,
    val region: String,
    val summary: String,
    val highlights: List<String>,
    val localFood: List<String>,
    val images: List<ImageRef>,
    val location: GeoPoint,
    val interests: Set<TravelInterest>,
    val rating: Double?,
    val ratingCount: Int?,
    val bestTimeToVisit: String,
    val currency: CurrencyCode,
    val languages: List<String>,
    val featured: Boolean = false,
    /** Curated things to do, in editorial order. */
    val attractions: List<Attraction> = emptyList(),
    /** Typical mid-range spend per person per day (in the catalog's currency, INR), by category. Basis for expense estimates. */
    val dailyCost: Map<ExpenseCategory, Money> = emptyMap(),
) {
    val coverImage: ImageRef? get() = images.firstOrNull()
}

/** A destination paired with the current user's saved state, as most lists need both. */
data class SaveableDestination(val destination: Destination, val isSaved: Boolean)

data class SavedDestination(val destination: Destination, val savedAt: java.time.Instant)

/** A curated attraction or activity at a destination. Coordinates are approximate (editorial). */
data class Attraction(
    val id: String,
    val destinationId: String,
    val name: String,
    val category: ActivityCategory,
    val location: GeoPoint,
    val durationMinutes: Int,
    val estimatedCost: Money,
    val description: String,
)
