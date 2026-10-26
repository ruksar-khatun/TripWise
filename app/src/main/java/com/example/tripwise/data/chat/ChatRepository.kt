package com.example.tripwise.data.chat

import android.util.Log
import com.example.tripwise.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatRepository {
    private val apiService = ChatApiService.create()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    init {
        resetToWelcomeMessage()
    }

    fun resetToWelcomeMessage() {
        _messages.value = listOf(
            ChatMessage(
                text = "Hello! I'm your TripWise AI Travel Assistant. 🌏✈️\n\nAsk me about Asian destinations, custom 3-day itineraries, local street food recommendations, or budget planning tips!",
                isUser = false
            )
        )
    }

    suspend fun sendMessage(
        userPrompt: String,
        contextDto: TripWiseContextDto
    ): Result<String> {
        // Build conversation history from previous valid messages BEFORE adding the new prompt
        val historyDtoList = _messages.value
            .filter { !it.isError }
            .takeLast(10) // Limit conversation memory window
            .map {
                ChatMessageDto(
                    role = if (it.isUser) "user" else "assistant",
                    content = it.text
                )
            }

        // Avoid adding duplicate user message bubble if retrying same prompt after an error
        val currentList = _messages.value
        val lastMsg = currentList.lastOrNull()
        val isDuplicatePrompt = lastMsg != null && lastMsg.isUser && lastMsg.text == userPrompt

        if (!isDuplicatePrompt) {
            val userMessage = ChatMessage(text = userPrompt, isUser = true)
            _messages.value = _messages.value.filter { !it.isError } + userMessage
        } else {
            // Remove previous error message if retrying
            _messages.value = _messages.value.filter { !it.isError }
        }

        val requestPayload = ChatApiRequest(
            message = userPrompt,
            conversationHistory = historyDtoList,
            context = contextDto
        )

        val backendUrl = BuildConfig.CHAT_BACKEND_URL.trimEnd('/') + "/api/chat"

        return try {
            val response = apiService.sendMessage(backendUrl, requestPayload)
            
            if (response.isSuccessful) {
                val body = response.body()
                val replyText = body?.reply
                if (!replyText.isNullOrEmpty()) {
                    val aiMessage = ChatMessage(text = replyText, isUser = false)
                    _messages.value = _messages.value + aiMessage
                    Result.success(replyText)
                } else {
                    val err = body?.error ?: "Backend returned empty response."
                    addErrorMessage("AI Assistant Error: $err")
                    Result.failure(Exception(err))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = if (response.code() == 404 || response.code() == 502) {
                    "Backend AI server is offline or unreachable at $backendUrl.\n\nPlease start the TripWise Node.js backend server (`node server/index.js`) and ensure your API key is configured in the server environment."
                } else {
                    "API Error (${response.code()}): ${errorBody ?: response.message()}"
                }
                
                addErrorMessage(errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Network call failed", e)
            val detailedError = "Unable to connect to TripWise AI backend.\n\n" +
                    "• Target URL: $backendUrl\n" +
                    "• Error: ${e.localizedMessage ?: "Network Connection Timeout"}\n\n" +
                    "Setup Instructions:\n" +
                    "1. Start reference server: `cd server && npm install && npm start`\n" +
                    "2. Configure `GEMINI_API_KEY` in `server/.env` file."

            addErrorMessage(detailedError)
            Result.failure(e)
        }
    }

    private fun addErrorMessage(text: String) {
        val errorMsg = ChatMessage(
            text = text,
            isUser = false,
            isError = true
        )
        _messages.value = _messages.value + errorMsg
    }

    fun clearChatHistory() {
        resetToWelcomeMessage()
    }
}
