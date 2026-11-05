package com.tripwise.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/** A TripMate AI conversation. Local only (chat history is never synced). */
@Entity(tableName = "chat_conversations", indices = [Index("updated_at")])
data class ChatConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "destination_id") val destinationId: String?,
    @ColumnInfo(name = "trip_id") val tripId: String?,
    /** BudgetLevel name, or null. */
    @ColumnInfo(name = "budget_level") val budgetLevel: String?,
    val days: Int?,
    val travellers: Int?,
    /** Comma-separated TravelInterest names ("" when none). */
    val interests: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [ForeignKey(ChatConversationEntity::class, ["id"], ["conversation_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("conversation_id")],
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    /** ChatRole name. */
    val role: String,
    val text: String,
    /** JSON array of recommended places, or null. Encoded by the data layer. */
    @ColumnInfo(name = "places_json") val placesJson: String?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
)

/** Conversation row plus its latest message text, for the history list. */
data class ChatConversationSummary(
    @androidx.room.Embedded val conversation: ChatConversationEntity,
    @ColumnInfo(name = "preview") val preview: String?,
)
