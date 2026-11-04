package com.tripwise.core.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ModelTest {
    private val jpy = CurrencyCode("JPY")

    private fun trip(start: String, end: String) = Trip(
        "t", "T", null, "Kyoto", null, LocalDate.parse(start), LocalDate.parse(end), 2, null, null,
    )

    @Test fun `day count is inclusive`() = assertEquals(7, trip("2026-03-14", "2026-03-20").dayCount)

    @Test fun `trip status relative to today`() {
        val t = trip("2026-03-14", "2026-03-20")
        assertEquals(TripStatus.UPCOMING, t.statusOn(LocalDate.parse("2026-03-01")))
        assertEquals(TripStatus.ONGOING, t.statusOn(LocalDate.parse("2026-03-14")))
        assertEquals(TripStatus.ONGOING, t.statusOn(LocalDate.parse("2026-03-20")))
        assertEquals(TripStatus.PAST, t.statusOn(LocalDate.parse("2026-03-21")))
        assertEquals(13, t.daysUntilStart(LocalDate.parse("2026-03-01")))
    }

    @Test fun `budget sums spending and fraction`() {
        val b = BudgetSummary(
            "t", Money(100_000, jpy),
            listOf(
                CategoryBudget(ExpenseCategory.FOOD, Money(40_000, jpy), Money(30_000, jpy)),
                CategoryBudget(ExpenseCategory.ACCOMMODATION, Money(60_000, jpy), Money(50_000, jpy)),
            ),
        )
        assertEquals(Money(80_000, jpy), b.spent)
        assertEquals(0.8f, b.spentFraction, 0.0001f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `adding different currencies fails`() {
        Money(1, jpy) + Money(1, CurrencyCode("INR"))
    }
}
