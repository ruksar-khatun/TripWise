package com.tripwise.core.data.repository

import com.tripwise.core.common.error.AppError
import com.tripwise.core.model.ActivityCategory
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.Destination
import com.tripwise.core.model.ExpenseCategory
import com.tripwise.core.model.ItineraryPlan
import com.tripwise.core.model.ItineraryRequest
import com.tripwise.core.model.Money
import com.tripwise.core.model.PlanDay
import com.tripwise.core.model.PlanItem
import com.tripwise.core.model.PlanSource
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.model.TravelPace
import java.time.LocalTime

/**
 * On-device port of the backend's curated itinerary template (backend/src/ai/itinerary.ts, templateItinerary),
 * used when the server can't be reached so planning works with no backend. Same rules: attractions in catalog order
 * (food first if the traveller likes food), a lunch at noon or later, 30 minutes between stops.
 */
internal object OfflinePlanner {
    /** Errors where no server answer is coming, so a curated plan is better than an error. */
    fun canReplace(error: AppError): Boolean = when (error) {
        AppError.Network, AppError.Timeout, AppError.Unauthorized, AppError.NotConfigured, is AppError.Server, is AppError.Ai -> true
        else -> false
    }

    fun plan(d: Destination, r: ItineraryRequest): ItineraryPlan {
        val perDay = when (r.pace) { TravelPace.RELAXED -> 3; TravelPace.BALANCED -> 4; TravelPace.PACKED -> 5 }
        val foodFirst = TravelInterest.FOOD in r.interests
        val pool = if (foodFirst) d.attractions.sortedByDescending { it.category == ActivityCategory.FOOD } else d.attractions
        val inr = CurrencyCode("INR")
        val lunchCost = d.dailyCost[ExpenseCategory.FOOD]?.let { Money(it.amountMinor / 2, it.currency) } ?: Money(300_00, inr)
        val days = (0 until r.days).map { i ->
            var clock = 9 * 60
            val items = mutableListOf<PlanItem>()
            var slot = 0
            while (slot < perDay - 1 && pool.isNotEmpty()) {
                val a = pool[(i * (perDay - 1) + slot) % pool.size]
                if (slot == 1) {
                    clock = maxOf(clock, 12 * 60)
                    val dish = d.localFood.getOrNull((i + slot) % maxOf(1, d.localFood.size)) ?: "local specialities"
                    items += PlanItem(time(clock), "Lunch: $dish", ActivityCategory.FOOD, 75, lunchCost, null, null)
                    clock += 90
                }
                val duration = minOf(a.durationMinutes, 300)
                items += PlanItem(time(clock), a.name, a.category, duration, a.estimatedCost, a.description, a.id)
                clock += duration + 30
                slot++
            }
            PlanDay(i + 1, if (i == 0) "Arrive and explore ${d.name}" else "${d.name} highlights, day ${i + 1}", items)
        }
        return ItineraryPlan(
            destinationId = d.id,
            title = "${r.days} day${if (r.days > 1) "s" else ""} in ${d.name}",
            summary = "A ${r.pace.name.lowercase()} plan built from TripWise's curated highlights for ${d.name}.",
            source = PlanSource.TEMPLATE,
            days = days,
        )
    }

    private fun time(minutes: Int): LocalTime = LocalTime.of((minutes / 60).coerceAtMost(23), minutes % 60)
}
