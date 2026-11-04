package com.tripwise.core.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import com.tripwise.core.model.Money
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Currency
import java.util.Locale

/** Locale-aware formatting. All user-visible numbers, money and dates go through here. */
object Formatters {

    fun money(money: Money, locale: Locale): String {
        val currency = runCatching { Currency.getInstance(money.currency.code) }.getOrNull()
        val digits = currency?.defaultFractionDigits?.coerceAtLeast(0) ?: 2
        val value = BigDecimal.valueOf(money.amountMinor, digits)
        return NumberFormat.getCurrencyInstance(locale).apply {
            if (currency != null) this.currency = currency
            // Whole amounts read better in budgets; show fractions only when present.
            minimumFractionDigits = if (value.stripTrailingZeros().scale() <= 0) 0 else digits
            maximumFractionDigits = digits
        }.format(value)
    }

    /** "14 – 20 Mar", "28 Mar – 3 Apr", "28 Dec 2026 – 3 Jan 2027". */
    fun dateRange(start: LocalDate, end: LocalDate, locale: Locale, today: LocalDate = LocalDate.now()): String {
        val sameYear = start.year == end.year
        val showYear = !sameYear || start.year != today.year
        val dayMonth = DateTimeFormatter.ofPattern("d MMM", locale)
        val dayMonthYear = DateTimeFormatter.ofPattern("d MMM yyyy", locale)
        return when {
            start == end -> start.format(if (showYear) dayMonthYear else dayMonth)
            sameYear && start.month == end.month && !showYear -> "${start.dayOfMonth} – ${end.format(dayMonth)}"
            showYear -> "${start.format(if (sameYear) dayMonth else dayMonthYear)} – ${end.format(dayMonthYear)}"
            else -> "${start.format(dayMonth)} – ${end.format(dayMonth)}"
        }
    }

    fun dayLabel(date: LocalDate, locale: Locale): String =
        date.format(DateTimeFormatter.ofPattern("EEE d MMM", locale))

    fun time(time: LocalTime, locale: Locale): String =
        time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))

    fun percent(fraction: Float, locale: Locale): String =
        NumberFormat.getPercentInstance(locale).format(fraction.toDouble())
}

/** Current UI locale, for passing into [Formatters]. */
val currentLocale: Locale
    @Composable @ReadOnlyComposable
    get() = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
