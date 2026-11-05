package com.tripwise.core.ui.preview

import com.tripwise.core.model.ActivityCategory
import com.tripwise.core.model.BudgetSummary
import com.tripwise.core.model.CategoryBudget
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.Destination
import com.tripwise.core.model.ExpenseCategory
import com.tripwise.core.model.GeoPoint
import com.tripwise.core.model.ItineraryItem
import com.tripwise.core.model.Money
import com.tripwise.core.model.SaveableDestination
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.model.Trip
import com.tripwise.core.model.UserProfile
import java.time.LocalDate
import java.time.LocalTime

/** Static sample models for @Preview functions only (images are not loaded in previews). */
object PreviewData {
    val today: LocalDate = LocalDate.of(2026, 3, 2)
    private val jpy = CurrencyCode("INR")

    val destinations: List<Destination> = listOf(
        "Goa" to "India", "Jaipur" to "India", "Udaipur" to "India", "Leh-Ladakh" to "India",
    ).mapIndexed { i, (name, country) ->
        Destination(
            id = name.lowercase(), name = name, country = country, region = "Region",
            summary = "Palm-fringed beaches, Portuguese-era churches and spice plantations on the Konkan coast.",
            highlights = listOf("Palolem Beach", "Old Goa churches", "Fontainhas"),
            localFood = listOf("Fish curry rice", "Bebinca", "Xacuti"), images = emptyList(),
            location = GeoPoint(0.0, 0.0), interests = setOf(TravelInterest.BEACHES, TravelInterest.CULTURE),
            rating = null, ratingCount = null, bestTimeToVisit = "Nov – Feb",
            currency = CurrencyCode("INR"), languages = listOf("Konkani"),
        )
    }

    fun saveable(index: Int, saved: Boolean = false) = SaveableDestination(destinations[index], saved)

    val trip = Trip(
        "trip-jaipur", "Jaipur Weekend", "jaipur", "Jaipur", null,
        today.plusDays(12), today.plusDays(18), 2, Money(4_500_000, jpy), "Light show at Amber Fort.",
    )

    val budget = BudgetSummary(
        "trip-jaipur", Money(4_500_000, jpy),
        listOf(
            CategoryBudget(ExpenseCategory.ACCOMMODATION, Money(1_800_000, jpy), Money(1_500_000, jpy)),
            CategoryBudget(ExpenseCategory.FOOD, Money(1_000_000, jpy), Money(1_100_000, jpy)),
            CategoryBudget(ExpenseCategory.TRANSPORT, Money(700_000, jpy), Money(400_000, jpy)),
        ),
    )

    val itinerary = listOf(
        ItineraryItem("1", "trip-jaipur", trip.startDate, LocalTime.of(9, 0), "Amber Fort", ActivityCategory.SIGHTSEEING, Money(10_000, jpy), true),
        ItineraryItem("2", "trip-jaipur", trip.startDate, LocalTime.of(12, 30), "Lunch: Dal baati churma", ActivityCategory.FOOD, Money(40_000, jpy), false),
    )

    val profile = UserProfile("u1", "Alex Traveller", "alex@example.com", null, CurrencyCode("INR"))
}
