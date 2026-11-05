package com.tripwise.core.data

import com.tripwise.core.common.error.AppError
import com.tripwise.core.data.repository.OfflinePlanner
import com.tripwise.core.model.ActivityCategory
import com.tripwise.core.model.Attraction
import com.tripwise.core.model.BudgetLevel
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.ExpenseCategory
import com.tripwise.core.model.GeoPoint
import com.tripwise.core.model.ItineraryRequest
import com.tripwise.core.model.Money
import com.tripwise.core.model.PlanSource
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.model.TravelPace
import com.tripwise.core.testing.data.TestData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class OfflinePlannerTest {
    private val inr = CurrencyCode("INR")
    private fun attraction(id: String, c: ActivityCategory) =
        Attraction(id, "jaipur", "Place $id", c, GeoPoint(0.0, 0.0), 90, Money(100_00, inr), "About $id")
    private val jaipur = TestData.destination("jaipur", name = "Jaipur").copy(
        localFood = listOf("Dal baati churma", "Laal maas"),
        attractions = listOf(attraction("a", ActivityCategory.SIGHTSEEING), attraction("b", ActivityCategory.FOOD), attraction("c", ActivityCategory.SHOPPING)),
        dailyCost = mapOf(ExpenseCategory.FOOD to Money(1_600_00, inr)),
    )

    @Test fun `builds the requested days with a lunch at noon or later, like the server template`() {
        val plan = OfflinePlanner.plan(jaipur, ItineraryRequest("jaipur", 3, 2, TravelPace.BALANCED, BudgetLevel.MID, setOf(TravelInterest.FOOD)))
        assertEquals(PlanSource.TEMPLATE, plan.source)
        assertEquals(listOf(1, 2, 3), plan.days.map { it.day })
        assertEquals("3 days in Jaipur", plan.title)
        val day1 = plan.days.first().items
        assertEquals(4, day1.size) // balanced pace: 3 stops + lunch
        assertEquals("Place b", day1.first().title) // likes food → food attraction first
        val lunch = day1.single { it.title.startsWith("Lunch: ") }
        assertTrue(!lunch.startTime.isBefore(LocalTime.NOON))
        assertEquals(Money(800_00, inr), lunch.estimatedCost) // half the daily food budget
        assertTrue(day1.zipWithNext().all { (a, b) -> a.startTime < b.startTime })
    }

    @Test fun `replaces only errors where no server answer is coming`() {
        assertTrue(OfflinePlanner.canReplace(AppError.Network))
        assertTrue(OfflinePlanner.canReplace(AppError.Unauthorized))
        assertTrue(OfflinePlanner.canReplace(AppError.NotConfigured))
        assertFalse(OfflinePlanner.canReplace(AppError.Validation()))
        assertFalse(OfflinePlanner.canReplace(AppError.NotFound))
    }
}
