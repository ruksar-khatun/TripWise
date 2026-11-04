package com.tripwise.feature.assistant

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwise.core.common.error.AppError
import com.tripwise.core.domain.repository.AssistantEvent
import com.tripwise.core.domain.repository.AssistantRepository
import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.domain.repository.TripRepository
import com.tripwise.core.model.AssistantContext
import com.tripwise.core.model.ChatMessage
import com.tripwise.core.model.ChatRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AssistantUiState(
    val contextTitle: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    /** Id of the assistant message currently streaming, if any. */
    val streamingId: String? = null,
    /** Last failure; the screen offers Retry, which re-sends the last user message. */
    val error: AppError? = null,
) {
    val sending: Boolean get() = streamingId != null
    val canSend: Boolean get() = input.isNotBlank() && !sending
}

sealed interface AssistantAction {
    data class InputChanged(val text: String) : AssistantAction
    data object Send : AssistantAction
    data class SendSuggestion(val text: String) : AssistantAction
    data object Retry : AssistantAction
    data object Stop : AssistantAction
}

@HiltViewModel
class AssistantViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val assistant: AssistantRepository,
    private val trips: TripRepository,
    private val destinations: DestinationRepository,
) : ViewModel() {
    private val tripId: String? = savedStateHandle["tripId"]
    private val destinationId: String? = savedStateHandle["destinationId"]
    private var context = AssistantContext(destinationId = destinationId)
    private var job: Job? = null

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val trip = tripId?.let { trips.observeTrip(it).firstOrNull() }
            val destinationName = (destinationId ?: trip?.destinationId)?.let { destinations.observeDestination(it).firstOrNull()?.name }
            context = AssistantContext(destinationId = destinationId ?: trip?.destinationId, trip = trip)
            _uiState.update { it.copy(contextTitle = trip?.title ?: destinationName) }
        }
    }

    fun onAction(action: AssistantAction) {
        when (action) {
            is AssistantAction.InputChanged -> _uiState.update { it.copy(input = action.text.take(MAX_INPUT)) }
            AssistantAction.Send -> _uiState.value.input.trim().takeIf { it.isNotEmpty() && !_uiState.value.sending }?.let(::send)
            is AssistantAction.SendSuggestion -> if (!_uiState.value.sending) send(action.text)
            AssistantAction.Retry -> retry()
            AssistantAction.Stop -> stop()
        }
    }

    private fun send(text: String) {
        val user = ChatMessage(UUID.randomUUID().toString(), ChatRole.USER, text)
        _uiState.update { it.copy(messages = it.messages + user, input = "", error = null) }
        stream()
    }

    private fun retry() {
        if (_uiState.value.sending) return
        // Drop a partial/empty assistant reply, then ask again for the last user message.
        _uiState.update { s -> s.copy(messages = s.messages.dropLastWhile { it.role == ChatRole.ASSISTANT }, error = null) }
        if (_uiState.value.messages.lastOrNull()?.role == ChatRole.USER) stream()
    }

    private fun stop() {
        job?.cancel()
        _uiState.update { it.copy(streamingId = null) }
    }

    private fun stream() {
        val replyId = UUID.randomUUID().toString()
        val history = _uiState.value.messages
        _uiState.update { it.copy(messages = it.messages + ChatMessage(replyId, ChatRole.ASSISTANT, ""), streamingId = replyId) }
        job = viewModelScope.launch {
            assistant.reply(history, context).collect { event ->
                when (event) {
                    is AssistantEvent.Delta -> _uiState.update { s ->
                        s.copy(messages = s.messages.map { if (it.id == replyId) it.copy(text = it.text + event.text) else it })
                    }
                    AssistantEvent.Done -> _uiState.update { it.copy(streamingId = null) }
                    is AssistantEvent.Failed -> _uiState.update { s ->
                        s.copy(
                            streamingId = null,
                            error = event.error,
                            // Keep partial text if any arrived; remove an empty placeholder bubble.
                            messages = s.messages.filterNot { it.id == replyId && it.text.isBlank() },
                        )
                    }
                }
            }
            _uiState.update { if (it.streamingId == replyId) it.copy(streamingId = null) else it }
        }
    }

    private companion object { const val MAX_INPUT = 2_000 }
}
