package com.tripwise.core.model

data class UserProfile(
    val id: String,
    val displayName: String,
    val email: String,
    val photo: ImageRef?,
    val homeCurrency: CurrencyCode,
)

data class TravelStats(val trips: Int, val saved: Int, val countries: Int)

/** Who is using the app right now. */
sealed interface SessionState {
    /** Still reading stored credentials at startup. */
    data object Unknown : SessionState

    /** @param expired true when the session ended on its own (refresh token rejected), not by the user. */
    data class SignedOut(val expired: Boolean = false) : SessionState

    data class SignedIn(val user: UserProfile) : SessionState
}

/** Reserved id of the on-device guest profile (no account, no tokens; everything stays on this phone). */
const val GUEST_USER_ID = "guest"

val UserProfile.isGuest: Boolean get() = id == GUEST_USER_ID

fun guestProfile(): UserProfile = UserProfile(GUEST_USER_ID, "Guest traveller", "Not signed in", null, CurrencyCode("INR"))
