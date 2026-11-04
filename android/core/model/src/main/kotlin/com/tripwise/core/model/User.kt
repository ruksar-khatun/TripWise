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

enum class ChatRole { USER, ASSISTANT }

data class ChatMessage(val id: String, val role: ChatRole, val text: String)

/** What the assistant conversation is about (sent to the backend as context). */
data class AssistantContext(val destinationId: String? = null, val trip: Trip? = null)
