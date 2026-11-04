package com.tripwise.core.testing.data

import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.Destination
import com.tripwise.core.model.GeoPoint
import com.tripwise.core.model.ImageRef
import com.tripwise.core.model.Money
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.model.Trip
import com.tripwise.core.model.UserProfile
import java.time.LocalDate

/** Small, stable fixtures for unit tests. */
object TestData {
    fun destination(
        id: String,
        name: String = id.replaceFirstChar { it.uppercase() },
        country: String = "Country",
        interests: Set<TravelInterest> = emptySet(),
    ) = Destination(
        id = id, name = name, country = country, region = "Region", summary = "Summary",
        highlights = listOf("Highlight"), localFood = listOf("Food"), images = listOf(ImageRef("https://example.com/$id.jpg")),
        location = GeoPoint(0.0, 0.0), interests = interests, rating = 4.5, ratingCount = 100,
        bestTimeToVisit = "Any time", currency = CurrencyCode("USD"), languages = listOf("English"),
    )

    fun trip(id: String, start: LocalDate, days: Long = 3, destinationId: String? = null) = Trip(
        id = id, title = "Trip $id", destinationId = destinationId, destinationName = "Place", coverImage = null,
        startDate = start, endDate = start.plusDays(days - 1), travellers = 2,
        budget = Money(100_000, CurrencyCode("USD")), notes = null,
    )

    val profile = UserProfile("user-1", "Test User", "test@example.com", null, CurrencyCode("INR"))
}
