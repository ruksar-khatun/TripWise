package com.tripwise.core.data.repository

import com.tripwise.core.data.mapper.enumOr
import com.tripwise.core.database.dao.ChatDao
import com.tripwise.core.database.entity.ChatConversationEntity
import com.tripwise.core.database.entity.ChatMessageEntity
import com.tripwise.core.domain.repository.ChatHistoryRepository
import com.tripwise.core.model.BudgetLevel
import com.tripwise.core.model.ChatConversation
import com.tripwise.core.model.ChatMessage
import com.tripwise.core.model.ChatRole
import com.tripwise.core.model.PlaceKind
import com.tripwise.core.model.PlaceSuggestion
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.model.TripPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.time.Clock
import java.util.UUID
import javax.inject.Inject

@Serializable
internal data class StoredPlace(
    val name: String,
    val kind: String,
    val area: String = "",
    val type: String = "",
    val priceRange: String = "",
    val description: String = "",
)

internal class DefaultChatHistoryRepository @Inject constructor(
    private val dao: ChatDao,
    private val clock: Clock,
) : ChatHistoryRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val placesSerializer = ListSerializer(StoredPlace.serializer())

    override fun observeConversations(): Flow<List<ChatConversation>> =
        dao.observeConversations().map { list -> list.map { it.conversation.toModel(it.preview) } }

    override suspend fun conversation(id: String): ChatConversation? = dao.conversation(id)?.toModel(null)

    override suspend fun messages(conversationId: String): List<ChatMessage> = dao.messages(conversationId).map { e ->
        ChatMessage(e.id, enumOr(e.role, ChatRole.ASSISTANT), e.text, e.createdAt, decodePlaces(e.placesJson))
    }

    override suspend fun create(title: String, destinationId: String?, tripId: String?, preferences: TripPreferences): String {
        val now = clock.instant()
        val id = UUID.randomUUID().toString()
        dao.upsertConversation(
            ChatConversationEntity(id, title.trim().take(MAX_TITLE).ifEmpty { "TripMate AI chat" }, destinationId, tripId,
                preferences.budgetLevel?.name, preferences.days, preferences.travellers, preferences.interests.encode(), now, now),
        )
        return id
    }

    override suspend fun save(conversationId: String, message: ChatMessage) {
        val now = clock.instant()
        dao.saveMessage(
            ChatMessageEntity(
                message.id, conversationId, message.role.name, message.text,
                message.places.takeIf { it.isNotEmpty() }?.let { places ->
                    json.encodeToString(placesSerializer, places.map { StoredPlace(it.name, it.kind.name, it.area, it.type, it.priceRange, it.description) })
                },
                message.createdAt.takeIf { it.epochSecond > 0 } ?: now,
            ),
            now,
        )
    }

    override suspend fun updatePreferences(conversationId: String, preferences: TripPreferences) {
        val current = dao.conversation(conversationId) ?: return
        dao.upsertConversation(
            current.copy(
                budgetLevel = preferences.budgetLevel?.name, days = preferences.days, travellers = preferences.travellers,
                interests = preferences.interests.encode(),
            ),
        )
    }

    override suspend fun delete(conversationId: String) = dao.deleteConversation(conversationId)
    override suspend fun deleteAll() = dao.deleteAll()

    private fun decodePlaces(raw: String?): List<PlaceSuggestion> = raw?.let {
        runCatching { json.decodeFromString(placesSerializer, it) }.getOrNull()
    }.orEmpty().map { PlaceSuggestion(it.name, enumOr(it.kind, PlaceKind.FOOD), it.area, it.type, it.priceRange, it.description) }

    private fun ChatConversationEntity.toModel(preview: String?) = ChatConversation(
        id = id,
        title = title,
        destinationId = destinationId,
        tripId = tripId,
        preferences = TripPreferences(
            budgetLevel = budgetLevel?.let { enumOrNull<BudgetLevel>(it) },
            days = days,
            travellers = travellers,
            interests = interests.split(',').filter(String::isNotBlank).mapNotNull { enumOrNull<TravelInterest>(it) }.toSet(),
        ),
        updatedAt = updatedAt,
        preview = preview,
    )

    private fun Set<TravelInterest>.encode() = map { it.name }.sorted().joinToString(",")

    private companion object { const val MAX_TITLE = 60 }
}

private inline fun <reified E : Enum<E>> enumOrNull(value: String): E? = enumValues<E>().firstOrNull { it.name == value }
