package com.tripwise.core.model

data class UserProfile(
    val displayName: String,
    val email: String,
    val photo: ImageRef?,
    val homeCurrency: CurrencyCode,
)

data class TravelStats(val trips: Int, val saved: Int, val countries: Int)
