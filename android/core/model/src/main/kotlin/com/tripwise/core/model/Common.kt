package com.tripwise.core.model

/** ISO-4217 currency code, e.g. "INR", "JPY". */
@JvmInline
value class CurrencyCode(val code: String)

/**
 * Money is always stored in minor units (paise, cents, yen) with its currency, never as a Double.
 * [fractionDigits] follows ISO-4217 (JPY = 0, INR/USD = 2).
 */
data class Money(val amountMinor: Long, val currency: CurrencyCode) {
    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Currency mismatch: $currency vs ${other.currency}" }
        return copy(amountMinor = amountMinor + other.amountMinor)
    }

    companion object {
        fun zero(currency: CurrencyCode) = Money(0, currency)
    }
}

data class GeoPoint(val latitude: Double, val longitude: Double)

/** An image plus the credit its licence asks for. */
data class ImageRef(
    val url: String,
    val attribution: ImageAttribution? = null,
)

data class ImageAttribution(val author: String, val sourceUrl: String, val license: String? = null)
