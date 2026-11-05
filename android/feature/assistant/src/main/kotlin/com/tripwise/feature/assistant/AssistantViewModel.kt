package com.tripwise.feature.assistant

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.domain.repository.AssistantEvent
import com.tripwise.core.domain.repository.AssistantRepository
import com.tripwise.core.domain.repository.ChatHistoryRepository
import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.domain.repository.TripRepository
import com.tripwise.core.model.AssistantContext
import com.tripwise.core.model.ChatMessage
import com.tripwise.core.model.ChatRole
import com.tripwise.core.model.PlaceKind
import com.tripwise.core.model.Trip
import com.tripwise.core.model.TripPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.util.UUID
import javax.inject.Inject

data class AssistantUiState(
    /** False while the saved conversation / trip context is loading. */
    val ready: Boolean = false,
    val destinationId: String? = null,
    val destinationName: String? = null,
    val tripId: String? = null,
    val tripTitle: String? = null,
    val preferences: TripPreferences = TripPreferences(),
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    /** Id of the assistant message currently streaming, if any. */
    val streamingId: String? = null,
    /** Set while structured stays/restaurants are being fetched. */
    val recommending: PlaceKind? = null,
    /** Last failure; Retry repeats what failed (chat reply or recommendations). */
    val error: AppError? = null,
) {
    val contextTitle: String? get() = tripTitle ?: destinationName
    val busy: Boolean get() = streamingId != null || recommending != null
    val sending: Boolean get() = busy
    val canSend: Boolean get() = ready && input.isNotBlank() && !busy
}

sealed interface AssistantAction {
    data class InputChanged(val text: String) : AssistantAction
    data object Send : AssistantAction
    data class SendSuggestion(val text: String) : AssistantAction
    /** Stays or restaurants: structured cards when a destination is known, otherwise a normal question. */
    data class Recommend(val kind: PlaceKind) : AssistantAction
    data class UpdatePreferences(val preferences: TripPreferences) : AssistantAction
    data object Retry : AssistantAction
    data object Stop : AssistantAction
    /** Starts a fresh conversation about the same destination/trip. */
    data object NewChat : AssistantAction
}

/** What a retry should repeat. */
private sealed interface Pending {
    data object Reply : Pending
    data class Places(val kind: PlaceKind) : Pending
}

/**
 * TripMate AI. Opened with optional context (destination, trip), a saved conversation to resume,
 * a prompt to send straight away, or a starter (STAY / FOOD) that fetches recommendation cards.
 */
@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val assistant: AssistantRepository,
    private val history: ChatHistoryRepository,
    private val trips: TripRepository,
    private val destinations: DestinationRepository,
    private val clock: Clock,
) : ViewModel() {
    private var conversationId: String? = savedStateHandle[ARG_CONVERSATION]
    private var trip: Trip? = null
    private var job: Job? = null
    private var pending: Pending? = null
    /** Serialises saves so the first message creates exactly one conversation. */
    private val persistLock = Mutex()

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = conversationId?.let { history.conversation(it) }
            if (saved == null) conversationId = null
            val tripId = saved?.tripId ?: savedStateHandle.get<String>(ARG_TRIP)
            trip = tripId?.let { trips.observeTrip(it).firstOrNull() }
            val destinationId = saved?.destinationId ?: savedStateHandle.get<String>(ARG_DESTINATION) ?: trip?.destinationId
            val destinationName = destinationId?.let { destinations.observeDestination(it).firstOrNull()?.name } ?: trip?.destinationName
            val preferences = saved?.preferences ?: TripPreferences(days = trip?.dayCount?.coerceIn(1, 30), travellers = trip?.travellers)
            _uiState.update {
                it.copy(
                    ready = true,
                    destinationId = destinationId,
                    destinationName = destinationName,
                    tripId = trip?.id,
                    tripTitle = trip?.title,
                    preferences = preferences,
                    messages = saved?.let { c -> history.messages(c.id) }.orEmpty(),
                )
            }
            // One-shot launch arguments; cleared so a recreated screen doesn't send them again.
            savedStateHandle.get<String>(ARG_STARTER)?.let { s ->
                savedStateHandle.remove<String>(ARG_STARTER)
                PlaceKind.entries.firstOrNull { it.name == s }?.let(::recommend)
            }
            savedStateHandle.get<String>(ARG_PROMPT)?.takeIf { it.isNotBlank() }?.let { p ->
                savedStateHandle.remove<String>(ARG_PROMPT)
                send(p.take(MAX_INPUT))
            }
        }
    }

    fun onAction(action: AssistantAction) {
        val s = _uiState.value
        when (action) {
            is AssistantAction.InputChanged -> _uiState.update { it.copy(input = action.text.take(MAX_INPUT)) }
            AssistantAction.Send -> s.input.trim().takeIf { it.isNotEmpty() && s.ready && !s.busy }?.let(::send)
            is AssistantAction.SendSuggestion -> if (s.ready && !s.busy) send(action.text)
            is AssistantAction.Recommend -> if (s.ready && !s.busy) recommend(action.kind)
            is AssistantAction.UpdatePreferences -> updatePreferences(action.preferences)
            AssistantAction.Retry -> retry()
            AssistantAction.Stop -> stop()
            AssistantAction.NewChat -> newChat()
        }
    }

    private fun send(text: String) {
        val user = message(ChatRole.USER, text)
        _uiState.update { it.copy(messages = it.messages + user, input = "", error = null) }
        viewModelScope.launch { persist(user) }
        stream()
    }

    private fun recommend(kind: PlaceKind) {
        val s = _uiState.value
        val destinationId = s.destinationId
        if (destinationId == null) {
            // No catalog destination to anchor structured results: ask the model in plain text instead.
            send(if (kind == PlaceKind.STAY) DEFAULT_STAY_PROMPT else DEFAULT_FOOD_PROMPT)
            return
        }
        val name = s.destinationName ?: destinationId
        val user = message(ChatRole.USER, if (kind == PlaceKind.STAY) "Where should I stay in $name?" else "Where should I eat in $name?")
        _uiState.update { it.copy(messages = it.messages + user, input = "", error = null) }
        viewModelScope.launch { persist(user) }
        fetchPlaces(kind)
    }

    private fun fetchPlaces(kind: PlaceKind) {
        val s = _uiState.value
        val destinationId = s.destinationId ?: return
        pending = Pending.Places(kind)
        _uiState.update { it.copy(recommending = kind, error = null) }
        job = viewModelScope.launch {
            when (val r = assistant.recommend(destinationId, kind, s.preferences)) {
                is Outcome.Success -> {
                    val name = s.destinationName ?: destinationId
                    val intro = when {
                        r.value.isEmpty() -> "I couldn't find good matches right now. Try changing your budget or interests."
                        kind == PlaceKind.STAY -> "Here are places to stay in $name that fit your trip. Prices are typical estimates; check current rates on Google Maps."
                        else -> "Here are places to eat in $name worth trying. Prices are typical for two people; check opening hours on Google Maps."
                    }
                    val reply = message(ChatRole.ASSISTANT, intro).copy(places = r.value)
                    pending = null
                    _uiState.update { it.copy(messages = it.messages + reply, recommending = null) }
                    persist(reply)
                }
                is Outcome.Failure -> _uiState.update { it.copy(recommending = null, error = r.error) }
            }
        }
    }

    private fun retry() {
        val s = _uiState.value
        if (s.busy) return
        when (val p = pending) {
            is Pending.Places -> fetchPlaces(p.kind)
            else -> {
                // Drop a partial/empty assistant reply, then ask again for the last user message.
                val dropped = s.messages.takeLastWhile { it.role == ChatRole.ASSISTANT && it.places.isEmpty() }
                _uiState.update { it.copy(messages = it.messages.dropLast(dropped.size), error = null) }
                if (_uiState.value.messages.lastOrNull()?.role == ChatRole.USER) stream()
            }
        }
    }

    private fun stop() {
        job?.cancel()
        val s = _uiState.value
        s.messages.firstOrNull { it.id == s.streamingId }?.takeIf { it.text.isNotBlank() }?.let { partial ->
            viewModelScope.launch { persist(partial) }
        }
        _uiState.update { st -> st.copy(streamingId = null, recommending = null, messages = st.messages.filterNot { it.id == s.streamingId && it.text.isBlank() }) }
    }

    private fun newChat() {
        stop()
        conversationId = null
        pending = null
        savedStateHandle.remove<String>(ARG_CONVERSATION)
        _uiState.update { it.copy(messages = emptyList(), input = "", error = null) }
    }

    private fun updatePreferences(preferences: TripPreferences) {
        _uiState.update { it.copy(preferences = preferences) }
        conversationId?.let { id -> viewModelScope.launch { history.updatePreferences(id, preferences) } }
    }

    private fun stream() {
        pending = Pending.Reply
        val reply = message(ChatRole.ASSISTANT, "")
        val turns = _uiState.value.messages
        _uiState.update { it.copy(messages = it.messages + reply, streamingId = reply.id) }
        job = viewModelScope.launch {
            val s = _uiState.value
            val itinerary = s.tripId?.let { trips.observeItinerary(it).firstOrNull() }.orEmpty()
            val context = AssistantContext(destinationId = s.destinationId, trip = trip, itinerary = itinerary, preferences = s.preferences)
            assistant.reply(turns, context).collect { event ->
                when (event) {
                    is AssistantEvent.Delta -> _uiState.update { st ->
                        st.copy(messages = st.messages.map { if (it.id == reply.id) it.copy(text = it.text + event.text) else it })
                    }
                    AssistantEvent.Done -> {
                        pending = null
                        _uiState.update { it.copy(streamingId = null) }
                        _uiState.value.messages.firstOrNull { it.id == reply.id }?.let { persist(it) }
                    }
                    is AssistantEvent.Failed -> {
                        val partial = _uiState.value.messages.firstOrNull { it.id == reply.id }?.takeIf { it.text.isNotBlank() }
                        _uiState.update { st ->
                            st.copy(
                                streamingId = null,
                                error = event.error,
                                // Keep partial text if any arrived; remove an empty placeholder bubble.
                                messages = st.messages.filterNot { it.id == reply.id && it.text.isBlank() },
                            )
                        }
                        partial?.let { persist(it) }
                    }
                }
            }
            _uiState.update { if (it.streamingId == reply.id) it.copy(streamingId = null) else it }
        }
    }

    /** Saves to history, creating the conversation on its first message (no empty chats in history). */
    private suspend fun persist(message: ChatMessage) = persistLock.withLock {
        val id = conversationId ?: run {
            val s = _uiState.value
            val first = s.messages.firstOrNull { it.role == ChatRole.USER }?.text ?: message.text
            history.create(first.lineSequence().first().take(60), s.destinationId, s.tripId, s.preferences).also {
                conversationId = it
                savedStateHandle[ARG_CONVERSATION] = it
            }
        }
        history.save(id, message)
    }

    private fun message(role: ChatRole, text: String) = ChatMessage(UUID.randomUUID().toString(), role, text, clock.instant())

    internal companion object {
        const val ARG_TRIP = "tripId"
        const val ARG_DESTINATION = "destinationId"
        const val ARG_CONVERSATION = "conversationId"
        const val ARG_PROMPT = "prompt"
        const val ARG_STARTER = "starter"
        const val MAX_INPUT = 2_000
        const val DEFAULT_STAY_PROMPT = "Recommend places to stay for my trip in India, with typical prices."
        const val DEFAULT_FOOD_PROMPT = "Recommend restaurants and local food to try for my trip in India."
    }
}
