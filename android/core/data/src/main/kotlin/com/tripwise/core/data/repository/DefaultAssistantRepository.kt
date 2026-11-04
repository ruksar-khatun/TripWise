package com.tripwise.core.data.repository

import com.tripwise.core.domain.repository.AssistantEvent
import com.tripwise.core.domain.repository.AssistantRepository
import com.tripwise.core.model.AssistantContext
import com.tripwise.core.model.ChatMessage
import com.tripwise.core.model.ChatRole
import com.tripwise.core.network.datasource.AssistantNetworkDataSource
import com.tripwise.core.network.datasource.ChatContextDto
import com.tripwise.core.network.datasource.ChatMessageDto
import com.tripwise.core.network.datasource.ChatRequestDto
import com.tripwise.core.network.datasource.ChatStreamEvent
import com.tripwise.core.network.datasource.ChatTripDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Sends the most recent turns (bounded, to cap tokens/cost) plus optional trip/destination context. */
internal class DefaultAssistantRepository @Inject constructor(
    private val network: AssistantNetworkDataSource,
) : AssistantRepository {
    override fun reply(history: List<ChatMessage>, context: AssistantContext): Flow<AssistantEvent> {
        val turns = history.filter { it.text.isNotBlank() }.takeLast(MAX_TURNS)
            .dropWhile { it.role != ChatRole.USER } // API requires starting from a user turn
            .map { ChatMessageDto(if (it.role == ChatRole.USER) "user" else "assistant", it.text.take(4_000)) }
        val trip = context.trip?.let {
            ChatTripDto(it.title.take(80), it.destinationName.take(80), it.startDate.toString(), it.endDate.toString(), it.travellers.coerceIn(1, 20))
        }
        return network.chat(ChatRequestDto(turns, ChatContextDto(context.destinationId, trip))).map {
            when (it) {
                is ChatStreamEvent.Delta -> AssistantEvent.Delta(it.text)
                ChatStreamEvent.Done -> AssistantEvent.Done
                is ChatStreamEvent.Failed -> AssistantEvent.Failed(it.error)
            }
        }
    }

    private companion object { const val MAX_TURNS = 20 }
}
