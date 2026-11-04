package com.tripwise.core.domain.usecase

import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.domain.repository.CurrencyRepository
import com.tripwise.core.model.BudgetLevel
import com.tripwise.core.model.CostEstimate
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.Destination
import com.tripwise.core.model.ExchangeRates
import com.tripwise.core.model.ExpenseCategory
import com.tripwise.core.model.ItineraryItem
import com.tripwise.core.model.Money
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * Transparent, deterministic estimate (no AI): the destination's typical daily spend per person
 * × days × travellers × a style factor, with activities taking the larger of the daily allowance
 * and what's already planned in the itinerary. Long-distance flights are not included.
 */
class EstimateTripCostUseCase @Inject constructor(private val currency: CurrencyRepository) {

    suspend operator fun invoke(
        destination: Destination,
        days: Int,
        travellers: Int,
        level: BudgetLevel,
        itinerary: List<ItineraryItem>,
        target: CurrencyCode,
    ): Outcome<CostEstimate> {
        if (destination.dailyCostUsd.isEmpty()) return Outcome.Failure(AppError.NotFound)
        val rates = when (val r = currency.usdRates()) {
            is Outcome.Success -> r.value
            is Outcome.Failure -> return r
        }
        return estimate(destination, days, travellers, level, itinerary, target, rates)
    }

    companion object {
        fun factor(level: BudgetLevel) = when (level) {
            BudgetLevel.BUDGET -> 0.6
            BudgetLevel.MID -> 1.0
            BudgetLevel.LUXURY -> 2.2
        }

        fun estimate(
            destination: Destination,
            days: Int,
            travellers: Int,
            level: BudgetLevel,
            itinerary: List<ItineraryItem>,
            target: CurrencyCode,
            rates: ExchangeRates,
        ): Outcome<CostEstimate> {
            val rate = rates.usdTo[target.code] ?: return Outcome.Failure(AppError.NotFound)
            val people = travellers.coerceAtLeast(1)
            val f = factor(level)
            val usdPerCategory = destination.dailyCostUsd.mapValues { (category, perDay) ->
                val base = perDay.amountMinor / 100.0 * days * people * f
                if (category == ExpenseCategory.ACTIVITIES) {
                    // Itinerary costs are per person, USD (as generated) or already in target currency.
                    val planned = itinerary.sumOf { item ->
                        val cost = item.estimatedCost ?: return@sumOf 0.0
                        val inUsd = if (cost.currency.code == "USD") cost.amountMinor / 100.0
                        else (rates.usdTo[cost.currency.code])?.let { r -> toMajor(cost) / r } ?: 0.0
                        inUsd * people
                    }
                    maxOf(base, planned)
                } else {
                    base
                }
            }
            val perCategory = usdPerCategory.mapValues { (_, usd) -> money(usd * rate, target) }
            val total = Money(perCategory.values.sumOf { it.amountMinor }, target)
            return Outcome.Success(CostEstimate(perCategory, total, level, days, people, rates.live))
        }

        private fun digits(code: CurrencyCode) =
            runCatching { java.util.Currency.getInstance(code.code).defaultFractionDigits }.getOrDefault(2).coerceAtLeast(0)

        private fun toMajor(m: Money) = BigDecimal.valueOf(m.amountMinor, digits(m.currency)).toDouble()

        /** Rounds estimates to whole units: false precision isn't helpful for a budget. */
        fun money(major: Double, code: CurrencyCode): Money {
            val whole = BigDecimal.valueOf(major).setScale(0, RoundingMode.HALF_UP)
            return Money(whole.movePointRight(digits(code)).toLong(), code)
        }
    }
}
