package com.tripwise.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.tripwise.core.database.entity.ChatConversationEntity
import com.tripwise.core.database.entity.ChatConversationSummary
import com.tripwise.core.database.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface ChatDao {
    @Query(
        """
        SELECT c.*, (SELECT m.text FROM chat_messages m WHERE m.conversation_id = c.id AND m.text != ''
                     ORDER BY m.created_at DESC LIMIT 1) AS preview
        FROM chat_conversations c ORDER BY c.updated_at DESC
        """,
    )
    fun observeConversations(): Flow<List<ChatConversationSummary>>

    @Query("SELECT * FROM chat_conversations WHERE id = :id")
    suspend fun conversation(id: String): ChatConversationEntity?

    @Query("SELECT * FROM chat_messages WHERE conversation_id = :conversationId ORDER BY created_at ASC, rowid ASC")
    suspend fun messages(conversationId: String): List<ChatMessageEntity>

    @Upsert suspend fun upsertConversation(conversation: ChatConversationEntity)

    @Upsert suspend fun upsertMessage(message: ChatMessageEntity)

    @Query("UPDATE chat_conversations SET updated_at = :now WHERE id = :id")
    suspend fun touch(id: String, now: Instant)

    /** Saves a message and bumps its conversation to the top of the history. */
    @Transaction
    suspend fun saveMessage(message: ChatMessageEntity, now: Instant) {
        upsertMessage(message)
        touch(message.conversationId, now)
    }

    @Query("DELETE FROM chat_conversations WHERE id = :id")
    suspend fun deleteConversation(id: String)

    @Query("DELETE FROM chat_conversations")
    suspend fun deleteAll()
}
