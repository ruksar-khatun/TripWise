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
    private val jpy = CurrencyCode("JPY")

    val destinations: List<Destination> = listOf(
        "Bali" to "Indonesia", "Kyoto" to "Japan", "Santorini" to "Greece", "Swiss Alps" to "Switzerland",
    ).mapIndexed { i, (name, country) ->
        Destination(
            id = name.lowercase(), name = name, country = country, region = "Region",
            summary = "Volcanic highlands, terraced rice fields and surf beaches, with a living culture woven through daily life.",
            highlights = listOf("Sunrise hike", "Rice terraces", "Cliff temple at sunset"),
            localFood = listOf("Nasi campur", "Sate lilit", "Lawar"), images = emptyList(),
            location = GeoPoint(0.0, 0.0), interests = setOf(TravelInterest.BEACHES, TravelInterest.CULTURE),
            rating = 4.6 + i * 0.1, ratingCount = 1200 + i * 300, bestTimeToVisit = "Apr – Oct",
            currency = CurrencyCode("IDR"), languages = listOf("Indonesian"),
        )
    }

    fun saveable(index: Int, saved: Boolean = false) = SaveableDestination(destinations[index], saved)

    val trip = Trip(
        "trip-kyoto", "Kyoto in Spring", "kyoto", "Kyoto", null,
        today.plusDays(12), today.plusDays(18), 2, Money(130_000, jpy), "Book a tea ceremony.",
    )

    val budget = BudgetSummary(
        "trip-kyoto", Money(130_000, jpy),
        listOf(
            CategoryBudget(ExpenseCategory.ACCOMMODATION, Money(50_000, jpy), Money(40_000, jpy)),
            CategoryBudget(ExpenseCategory.FOOD, Money(25_000, jpy), Money(28_000, jpy)),
            CategoryBudget(ExpenseCategory.TRANSPORT, Money(20_000, jpy), Money(14_000, jpy)),
        ),
    )

    val itinerary = listOf(
        ItineraryItem("1", "trip-kyoto", trip.startDate, LocalTime.of(9, 0), "Fushimi Inari", ActivityCategory.SIGHTSEEING, Money(0, jpy), true),
        ItineraryItem("2", "trip-kyoto", trip.startDate, LocalTime.of(12, 30), "Nishiki Market", ActivityCategory.FOOD, Money(3_000, jpy), false),
    )

    val profile = UserProfile("Alex Traveller", "alex@example.com", null, CurrencyCode("INR"))
}
