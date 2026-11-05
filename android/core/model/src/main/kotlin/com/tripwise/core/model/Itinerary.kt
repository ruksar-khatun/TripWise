package com.tripwise.core.model

import java.time.LocalTime

enum class TravelPace { RELAXED, BALANCED, PACKED }
enum class BudgetLevel { BUDGET, MID, LUXURY }

data class ItineraryRequest(
    val destinationId: String,
    val days: Int,
    val travellers: Int,
    val pace: TravelPace,
    val budgetLevel: BudgetLevel,
    val interests: Set<TravelInterest>,
    val notes: String? = null,
)

enum class PlanSource { AI, TEMPLATE }

data class PlanItem(
    val startTime: LocalTime,
    val title: String,
    val category: ActivityCategory,
    val durationMinutes: Int,
    /** Per person (INR from the backend). */
    val estimatedCost: Money,
    val notes: String?,
    val attractionId: String?,
)

data class PlanDay(val day: Int, val theme: String, val items: List<PlanItem>)

/** A generated draft; nothing is saved until the user accepts it. */
data class ItineraryPlan(
    val destinationId: String,
    val title: String,
    val summary: String,
    val source: PlanSource,
    val days: List<PlanDay>,
)
