package com.tripwise.core.domain.repository

import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.model.AssistantContext
import com.tripwise.core.model.ChatMessage
import com.tripwise.core.model.PlaceKind
import com.tripwise.core.model.PlaceSuggestion
import com.tripwise.core.model.TripPreferences
import kotlinx.coroutines.flow.Flow

sealed interface AssistantEvent {
    data class Delta(val text: String) : AssistantEvent
    data object Done : AssistantEvent
    data class Failed(val error: AppError) : AssistantEvent
}

interface AssistantRepository {
    /** Streams the assistant's reply to [history] (whose last message is the user's). Never throws. */
    fun reply(history: List<ChatMessage>, context: AssistantContext): Flow<AssistantEvent>

    /** Structured stays or restaurants for a catalog destination. */
    suspend fun recommend(destinationId: String, kind: PlaceKind, preferences: TripPreferences, notes: String? = null): Outcome<List<PlaceSuggestion>>
}
