package com.tripwise.core.model

import java.time.Instant

enum class ChatRole { USER, ASSISTANT }

/**
 * One TripMate AI chat message. An assistant message can carry structured [places]
 * (stays or restaurants) instead of, or in addition to, its text.
 */
data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val text: String,
    val createdAt: Instant = Instant.EPOCH,
    val places: List<PlaceSuggestion> = emptyList(),
)

enum class PlaceKind { STAY, FOOD }

/** A recommended hotel / restaurant. Not verified: the app links it to Google Maps to check. */
data class PlaceSuggestion(
    val name: String,
    val kind: PlaceKind,
    val area: String,
    val type: String,
    val priceRange: String,
    val description: String,
)

/** Budget, length and interests the traveller sets in TripMate AI; every field is optional. */
data class TripPreferences(
    val budgetLevel: BudgetLevel? = null,
    val days: Int? = null,
    val travellers: Int? = null,
    val interests: Set<TravelInterest> = emptySet(),
) {
    val isEmpty: Boolean get() = budgetLevel == null && days == null && travellers == null && interests.isEmpty()
}

/** A saved TripMate AI conversation, optionally about a destination or a trip. */
data class ChatConversation(
    val id: String,
    val title: String,
    val destinationId: String?,
    val tripId: String?,
    val preferences: TripPreferences,
    val updatedAt: Instant,
    val preview: String?,
)

/** What the conversation is about (sent to the backend as context). */
data class AssistantContext(
    val destinationId: String? = null,
    val trip: Trip? = null,
    /** The trip's planned activities, summarised for the model. */
    val itinerary: List<ItineraryItem> = emptyList(),
    val preferences: TripPreferences = TripPreferences(),
)
