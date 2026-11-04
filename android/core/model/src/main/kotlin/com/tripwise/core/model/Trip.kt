package com.tripwise.core.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

data class Trip(
    val id: String,
    val title: String,
    val destinationId: String?,
    val destinationName: String,
    val coverImage: ImageRef?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val travellers: Int,
    val budget: Money?,
    val notes: String?,
) {
    /** Inclusive day count: a 14→20 trip is 7 days. */
    val dayCount: Int get() = (ChronoUnit.DAYS.between(startDate, endDate) + 1).toInt()

    fun statusOn(today: LocalDate): TripStatus = when {
        today.isBefore(startDate) -> TripStatus.UPCOMING
        today.isAfter(endDate) -> TripStatus.PAST
        else -> TripStatus.ONGOING
    }

    fun daysUntilStart(today: LocalDate): Long = ChronoUnit.DAYS.between(today, startDate)
}

enum class TripStatus { UPCOMING, ONGOING, PAST }

enum class ActivityCategory { SIGHTSEEING, FOOD, ACTIVITY, TRANSPORT, LODGING, SHOPPING, OTHER }

data class ItineraryItem(
    val id: String,
    val tripId: String,
    val date: LocalDate,
    val startTime: LocalTime?,
    val title: String,
    val category: ActivityCategory,
    val estimatedCost: Money?,
    val isDone: Boolean,
    val notes: String? = null,
)

enum class ExpenseCategory { TRANSPORT, ACCOMMODATION, FOOD, ACTIVITIES, LOCAL_TRAVEL, SHOPPING, OTHER }

data class CategoryBudget(val category: ExpenseCategory, val planned: Money, val spent: Money)

data class BudgetSummary(val tripId: String, val total: Money, val categories: List<CategoryBudget>) {
    val spent: Money get() = categories.fold(Money.zero(total.currency)) { acc, c -> acc + c.spent }

    /** 0f..n; above 1f means over budget. */
    val spentFraction: Float
        get() = if (total.amountMinor <= 0) 0f else spent.amountMinor.toFloat() / total.amountMinor
}
