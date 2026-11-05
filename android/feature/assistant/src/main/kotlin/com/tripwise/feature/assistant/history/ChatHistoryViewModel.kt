package com.tripwise.feature.assistant.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwise.core.domain.repository.ChatHistoryRepository
import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.model.ChatConversation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatHistoryItem(val conversation: ChatConversation, val destinationName: String?)

sealed interface ChatHistoryUiState {
    data object Loading : ChatHistoryUiState
    data class Ready(val items: List<ChatHistoryItem>) : ChatHistoryUiState
}

sealed interface ChatHistoryAction {
    data class Delete(val conversationId: String) : ChatHistoryAction
    data object DeleteAll : ChatHistoryAction
}

@HiltViewModel
class ChatHistoryViewModel @Inject constructor(
    private val history: ChatHistoryRepository,
    destinations: DestinationRepository,
) : ViewModel() {
    val uiState: StateFlow<ChatHistoryUiState> = combine(
        history.observeConversations(),
        destinations.observeDestinations("", null).onStart { emit(emptyList()) },
    ) { conversations, all ->
        val names = all.associate { it.id to it.name }
        ChatHistoryUiState.Ready(conversations.map { ChatHistoryItem(it, it.destinationId?.let(names::get)) }) as ChatHistoryUiState
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatHistoryUiState.Loading)

    fun onAction(action: ChatHistoryAction) {
        viewModelScope.launch {
            when (action) {
                is ChatHistoryAction.Delete -> history.delete(action.conversationId)
                ChatHistoryAction.DeleteAll -> history.deleteAll()
            }
        }
    }
}
