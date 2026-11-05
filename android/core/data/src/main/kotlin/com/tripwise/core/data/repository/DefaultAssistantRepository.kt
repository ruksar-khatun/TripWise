package com.tripwise.core.data.repository

import com.tripwise.core.common.result.Outcome
import com.tripwise.core.common.result.map
import com.tripwise.core.domain.repository.AssistantEvent
import com.tripwise.core.domain.repository.AssistantRepository
import com.tripwise.core.model.AssistantContext
import com.tripwise.core.model.BudgetLevel
import com.tripwise.core.model.ChatMessage
import com.tripwise.core.model.ChatRole
import com.tripwise.core.model.PlaceKind
import com.tripwise.core.model.PlaceSuggestion
import com.tripwise.core.model.TripPreferences
import com.tripwise.core.network.api.RecommendationApi
import com.tripwise.core.network.api.RecommendationRequestDto
import com.tripwise.core.network.datasource.AssistantNetworkDataSource
import com.tripwise.core.network.datasource.ChatContextDto
import com.tripwise.core.network.datasource.ChatMessageDto
import com.tripwise.core.network.datasource.ChatPreferencesDto
import com.tripwise.core.network.datasource.ChatRequestDto
import com.tripwise.core.network.datasource.ChatStreamEvent
import com.tripwise.core.network.datasource.ChatTripDto
import com.tripwise.core.network.error.ApiErrorMapper
import com.tripwise.core.network.error.call
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Sends the most recent turns (bounded, to cap tokens/cost) plus optional trip/destination/preference context. */
internal class DefaultAssistantRepository @Inject constructor(
    private val network: AssistantNetworkDataSource,
    private val recommendations: RecommendationApi,
    private val errors: ApiErrorMapper,
) : AssistantRepository {
    override fun reply(history: List<ChatMessage>, context: AssistantContext): Flow<AssistantEvent> =
        network.chat(buildChatRequest(history, context)).map {
            when (it) {
                is ChatStreamEvent.Delta -> AssistantEvent.Delta(it.text)
                ChatStreamEvent.Done -> AssistantEvent.Done
                is ChatStreamEvent.Failed -> AssistantEvent.Failed(it.error)
            }
        }

    override suspend fun recommend(destinationId: String, kind: PlaceKind, preferences: TripPreferences, notes: String?): Outcome<List<PlaceSuggestion>> =
        errors.call {
            recommendations.recommend(
                RecommendationRequestDto(
                    destinationId = destinationId,
                    kind = kind.name,
                    budgetLevel = (preferences.budgetLevel ?: BudgetLevel.MID).name,
                    travellers = (preferences.travellers ?: 2).coerceIn(1, 20),
                    interests = preferences.interests.map { it.name },
                    notes = notes?.trim()?.take(300)?.ifEmpty { null },
                ),
            )
        }.map { res ->
            res.places.filter { it.name.isNotBlank() }.map {
                PlaceSuggestion(it.name.trim(), kind, it.area.trim(), it.type.trim(), it.priceRangeInr.trim(), it.description.trim())
            }
        }

    internal companion object {
        const val MAX_TURNS = 20
        const val MAX_PLANNED_ITEMS = 40

        /** Pure mapping, unit-tested: history trimming, place cards as text, trip + preferences context. */
        fun buildChatRequest(history: List<ChatMessage>, context: AssistantContext): ChatRequestDto {
            val turns = history.map { it.copy(text = it.asPromptText()) }
                .filter { it.text.isNotBlank() }.takeLast(MAX_TURNS)
                .dropWhile { it.role != ChatRole.USER } // API requires starting from a user turn
                .map { ChatMessageDto(if (it.role == ChatRole.USER) "user" else "assistant", it.text.take(4_000)) }
            val trip = context.trip?.let { t ->
                ChatTripDto(
                    title = t.title.take(80),
                    destinationName = t.destinationName.take(80),
                    startDate = t.startDate.toString(),
                    endDate = t.endDate.toString(),
                    travellers = t.travellers.coerceIn(1, 20),
                    budgetInr = t.budget?.takeIf { it.currency.code == "INR" }?.let { it.amountMinor / 100 },
                    itinerary = context.itinerary.take(MAX_PLANNED_ITEMS).map { i ->
                        listOfNotNull(i.date.toString(), i.startTime?.toString(), i.title).joinToString(" ").take(120)
                    }.ifEmpty { null },
                )
            }
            val p = context.preferences
            val preferences = if (p.isEmpty) null else ChatPreferencesDto(
                budgetLevel = p.budgetLevel?.name,
                days = p.days?.coerceIn(1, 30),
                travellers = p.travellers?.coerceIn(1, 20),
                interests = p.interests.map { it.name }.sorted().ifEmpty { null },
            )
            return ChatRequestDto(turns, ChatContextDto(context.destinationId, trip, preferences))
        }

        /** Recommendation cards have no text of their own; the model still needs to know what it suggested. */
        private fun ChatMessage.asPromptText(): String = if (places.isEmpty()) text else buildString {
            if (text.isNotBlank()) appendLine(text)
            places.forEach { appendLine("- ${it.name} (${listOf(it.area, it.priceRange).filter(String::isNotBlank).joinToString(", ")})") }
        }.trim()
    }
}
