package com.example.tripwise.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripwise.data.AppState
import com.example.tripwise.data.chat.ChatMessage
import com.example.tripwise.data.chat.ChatRepository
import com.example.tripwise.data.chat.TripWiseContextDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val suggestedPrompts: List<String> = listOf(
        "Recommend a destination",
        "Plan a 3-day trip",
        "Find things to do",
        "Suggest local food",
        "Help me manage my budget",
        "Best time to visit"
    )
)

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var lastAttemptedPrompt: String? = null

    init {
        viewModelScope.launch {
            repository.messages.collectLatest { msgList ->
                _uiState.update { it.copy(messages = msgList) }
            }
        }
    }

    fun onInputTextChange(newText: String) {
        _uiState.update { it.copy(inputText = newText, errorMessage = null) }
    }

    fun onSendMessage(appState: AppState) {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty() || _uiState.value.isLoading) return

        executePromptSend(text, appState)
    }

    fun onSuggestedPromptClick(prompt: String, appState: AppState) {
        if (_uiState.value.isLoading) return
        executePromptSend(prompt, appState)
    }

    fun onRetry(appState: AppState) {
        val prompt = lastAttemptedPrompt
        if (!prompt.isNullOrEmpty() && !_uiState.value.isLoading) {
            executePromptSend(prompt, appState)
        }
    }

    private fun executePromptSend(prompt: String, appState: AppState) {
        lastAttemptedPrompt = prompt
        _uiState.update { it.copy(inputText = "", isLoading = true, errorMessage = null) }

        val contextDto = TripWiseContextDto(
            selectedCurrency = appState.selectedCurrency,
            userInterests = appState.userProfile.interests,
            travelStyle = appState.userProfile.travelStyle,
            savedPlacesCount = appState.userProfile.savedCount,
            selectedDestinationName = appState.selectedDestination.name,
            totalTripsPlanned = appState.userProfile.totalTripsPlanned
        )

        viewModelScope.launch {
            val result = repository.sendMessage(prompt, contextDto)
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    errorMessage = if (result.isFailure) result.exceptionOrNull()?.localizedMessage ?: "Network Error" else null
                )
            }
        }
    }

    fun onClearChat() {
        repository.clearChatHistory()
        _uiState.update { it.copy(errorMessage = null) }
    }
}
