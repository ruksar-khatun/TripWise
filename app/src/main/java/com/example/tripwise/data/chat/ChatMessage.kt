package com.example.tripwise.data.chat

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

data class TripWiseContextDto(
    val selectedCurrency: String? = null,
    val userInterests: List<String> = emptyList(),
    val travelStyle: String? = null,
    val savedPlacesCount: Int = 0,
    val selectedDestinationName: String? = null,
    val totalTripsPlanned: Int = 0
)

data class ChatMessageDto(
    val role: String, // "user" or "model"/"assistant"
    val content: String
)

data class ChatApiRequest(
    val message: String,
    val conversationHistory: List<ChatMessageDto> = emptyList(),
    val context: TripWiseContextDto? = null
)

data class ChatApiResponse(
    val reply: String?,
    val status: String? = "success",
    val error: String? = null
)
