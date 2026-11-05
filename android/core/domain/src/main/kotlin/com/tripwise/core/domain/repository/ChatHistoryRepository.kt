package com.tripwise.core.domain.repository

import com.tripwise.core.model.ChatConversation
import com.tripwise.core.model.ChatMessage
import com.tripwise.core.model.TripPreferences
import kotlinx.coroutines.flow.Flow

/** TripMate AI conversations saved on this device (cleared on sign-out). */
interface ChatHistoryRepository {
    fun observeConversations(): Flow<List<ChatConversation>>
    suspend fun conversation(id: String): ChatConversation?
    suspend fun messages(conversationId: String): List<ChatMessage>

    /** Creates a conversation and returns its id. */
    suspend fun create(title: String, destinationId: String?, tripId: String?, preferences: TripPreferences): String

    /** Inserts or replaces [message] (same id) and moves the conversation to the top. */
    suspend fun save(conversationId: String, message: ChatMessage)
    suspend fun updatePreferences(conversationId: String, preferences: TripPreferences)
    suspend fun delete(conversationId: String)
    suspend fun deleteAll()
}
