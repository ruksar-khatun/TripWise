package com.tripwise.core.domain

import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.domain.usecase.EstimateTripCostUseCase
import com.tripwise.core.model.ActivityCategory
import com.tripwise.core.model.BudgetLevel
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.Destination
import com.tripwise.core.model.ExchangeRates
import com.tripwise.core.model.ExpenseCategory
import com.tripwise.core.model.GeoPoint
import com.tripwise.core.model.ItineraryItem
import com.tripwise.core.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class EstimateTripCostTest {
    private val usd = CurrencyCode("USD")
    private val inr = CurrencyCode("INR")
    private val jpy = CurrencyCode("JPY")
    private val rates = ExchangeRates(mapOf("USD" to 1.0, "INR" to 80.0, "JPY" to 150.0), live = true)
    private val kyoto = Destination(
        "kyoto", "Kyoto", "Japan", "Asia", "", emptyList(), emptyList(), emptyList(), GeoPoint(0.0, 0.0), emptySet(),
        null, null, "", jpy, emptyList(),
        dailyCost = mapOf(ExpenseCategory.ACCOMMODATION to Money(9_000, usd), ExpenseCategory.FOOD to Money(4_500, usd), ExpenseCategory.ACTIVITIES to Money(2_000, usd)),
    )

    private fun item(cost: Money) = ItineraryItem("i", "t", LocalDate.of(2026, 1, 1), null, "x", ActivityCategory.ACTIVITY, cost, false)

    @Test fun `daily costs scale by days, travellers and style, converted to target currency`() {
        val e = (EstimateTripCostUseCase.estimate(kyoto, 3, 2, BudgetLevel.MID, emptyList(), inr, rates) as Outcome.Success).value
        // accommodation: 90 × 3 × 2 = 540 USD → 43,200 INR → 4,320,000 paise
        assertEquals(Money(4_320_000, inr), e.perCategory[ExpenseCategory.ACCOMMODATION])
        assertEquals(Money(2_160_000, inr), e.perCategory[ExpenseCategory.FOOD])
        assertEquals(Money(960_000, inr), e.perCategory[ExpenseCategory.ACTIVITIES])
        assertEquals(Money(7_440_000, inr), e.total)

        val luxury = (EstimateTripCostUseCase.estimate(kyoto, 3, 2, BudgetLevel.LUXURY, emptyList(), jpy, rates) as Outcome.Success).value
        assertEquals(Money(178_200, jpy), luxury.perCategory[ExpenseCategory.ACCOMMODATION]) // 540 × 2.2 × 150, JPY has no minor unit
    }

    @Test fun `planned itinerary costs raise activities when above the daily allowance`() {
        val plan = listOf(item(Money(25_000, usd)), item(Money(5_000, usd))) // $300 per person
        val e = (EstimateTripCostUseCase.estimate(kyoto, 1, 2, BudgetLevel.MID, plan, usd, rates) as Outcome.Success).value
        assertEquals(Money(60_000, usd), e.perCategory[ExpenseCategory.ACTIVITIES]) // 300 × 2 people > 20 × 1 × 2
    }

    @Test fun `rupee daily costs need no conversion for rupee trips`() {
        val goa = kyoto.copy(dailyCost = mapOf(ExpenseCategory.FOOD to Money(100_000, inr))) // ₹1,000 per day
        val e = (EstimateTripCostUseCase.estimate(goa, 3, 2, BudgetLevel.BUDGET, emptyList(), inr, rates) as Outcome.Success).value
        assertEquals(Money(360_000, inr), e.perCategory[ExpenseCategory.FOOD]) // 1,000 × 3 × 2 × 0.6
    }

    @Test fun `unknown target currency fails`() {
        val r = EstimateTripCostUseCase.estimate(kyoto, 1, 1, BudgetLevel.MID, emptyList(), CurrencyCode("XYZ"), rates)
        assertEquals(Outcome.Failure(AppError.NotFound), r)
        assertTrue(EstimateTripCostUseCase.factor(BudgetLevel.BUDGET) < 1)
    }
}
