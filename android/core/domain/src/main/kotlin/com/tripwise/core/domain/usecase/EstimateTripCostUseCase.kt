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
 * Transparent, deterministic estimate (no AI): the destination's typical daily spend per person (any currency)
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
        if (destination.dailyCost.isEmpty()) return Outcome.Failure(AppError.NotFound)
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
            val targetRate = rates.usdTo[target.code] ?: return Outcome.Failure(AppError.NotFound)
            /** Amount in [target] major units, or null if the source currency has no rate. */
            fun convert(m: Money): Double? = rates.usdTo[m.currency.code]?.let { rate -> toMajor(m) / rate * targetRate }

            val people = travellers.coerceAtLeast(1)
            val f = factor(level)
            val perCategory = mutableMapOf<ExpenseCategory, Money>()
            for ((category, perDay) in destination.dailyCost) {
                val daily = convert(perDay) ?: return Outcome.Failure(AppError.NotFound)
                var amount = daily * days * people * f
                if (category == ExpenseCategory.ACTIVITIES) {
                    // Planned itinerary costs are per person; use them when higher than the daily allowance.
                    val planned = itinerary.sumOf { item -> item.estimatedCost?.let(::convert) ?: 0.0 } * people
                    amount = maxOf(amount, planned)
                }
                perCategory[category] = money(amount, target)
            }
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
