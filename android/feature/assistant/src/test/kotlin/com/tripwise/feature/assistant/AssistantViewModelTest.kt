package com.tripwise.feature.assistant

import androidx.lifecycle.SavedStateHandle
import com.tripwise.core.common.error.AiFailure
import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.domain.repository.AssistantEvent
import com.tripwise.core.model.ActivityCategory
import com.tripwise.core.model.BudgetLevel
import com.tripwise.core.model.ChatMessage
import com.tripwise.core.model.ChatRole
import com.tripwise.core.model.ItineraryItem
import com.tripwise.core.model.PlaceKind
import com.tripwise.core.model.PlaceSuggestion
import com.tripwise.core.model.TripPreferences
import com.tripwise.core.testing.data.TestData
import com.tripwise.core.testing.fakes.FakeAssistantRepository
import com.tripwise.core.testing.fakes.FakeChatHistoryRepository
import com.tripwise.core.testing.fakes.FakeDestinationRepository
import com.tripwise.core.testing.fakes.FakeTripRepository
import com.tripwise.core.testing.rules.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class AssistantViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val jaipur = TestData.destination("jaipur", name = "Jaipur")
    private val repo = FakeAssistantRepository()
    private val history = FakeChatHistoryRepository()
    private val trip = TestData.trip("t1", LocalDate.of(2026, 12, 1), days = 4, destinationId = "jaipur")
    private val trips = FakeTripRepository(listOf(trip))
    private val clock = Clock.fixed(Instant.parse("2026-11-05T10:00:00Z"), ZoneOffset.UTC)
    private val place = PlaceSuggestion("Laxmi Misthan Bhandar", PlaceKind.FOOD, "Johari Bazaar", "Sweets", "₹400-800 for two", "Classic")

    private fun vm(args: Map<String, Any?> = emptyMap()) =
        AssistantViewModel(SavedStateHandle(args), repo, history, trips, FakeDestinationRepository(listOf(jaipur)), clock)

    @Test fun `streams a reply into one bubble and saves both messages to a new conversation`() = runTest {
        repo.events = listOf(AssistantEvent.Delta("Hel"), AssistantEvent.Delta("lo"), AssistantEvent.Done)
        val vm = vm()
        assertTrue(vm.uiState.value.ready)
        vm.onAction(AssistantAction.InputChanged("Hi"))
        vm.onAction(AssistantAction.Send)
        val s = vm.uiState.value
        assertEquals(listOf(ChatRole.USER to "Hi", ChatRole.ASSISTANT to "Hello"), s.messages.map { it.role to it.text })
        assertEquals("", s.input)
        assertFalse(s.busy)

        val saved = history.observeConversations().first().single()
        assertEquals("Hi", saved.title)
        assertEquals(listOf("Hi", "Hello"), history.messages(saved.id).map { it.text })
    }

    @Test fun `trip context sends destination, trip, planned items and default preferences`() = runTest {
        trips.items.value = listOf(ItineraryItem("i", "t1", trip.startDate, null, "Amber Fort", ActivityCategory.SIGHTSEEING, null, false))
        val vm = vm(mapOf("tripId" to "t1"))
        assertEquals("Trip t1", vm.uiState.value.contextTitle)
        assertEquals(TripPreferences(days = 4, travellers = trip.travellers), vm.uiState.value.preferences)
        vm.onAction(AssistantAction.SendSuggestion("Budget per day?"))
        val (_, ctx) = repo.requests.single()
        assertEquals("jaipur", ctx.destinationId)
        assertEquals("t1", ctx.trip?.id)
        assertEquals(listOf("Amber Fort"), ctx.itinerary.map { it.title })
        assertEquals(4, ctx.preferences.days)
    }

    @Test fun `launch prompt is sent once`() = runTest {
        val handle = SavedStateHandle(mapOf("destinationId" to "jaipur", "prompt" to "Plan 3 days"))
        val vm = AssistantViewModel(handle, repo, history, trips, FakeDestinationRepository(listOf(jaipur)), clock)
        assertEquals("Plan 3 days", vm.uiState.value.messages.first().text)
        assertNull(handle.get<String>("prompt"))
        assertEquals(1, repo.requests.size)
    }

    @Test fun `restaurant starter shows place cards and keeps them in history`() = runTest {
        repo.recommendation = Outcome.Success(listOf(place))
        val vm = vm(mapOf("destinationId" to "jaipur", "starter" to "FOOD"))
        val messages = vm.uiState.value.messages
        assertEquals(ChatRole.USER, messages[0].role)
        assertEquals(listOf(place), messages[1].places)
        assertEquals("jaipur" to PlaceKind.FOOD, repo.recommendRequests.single().let { it.first to it.second })
        assertNull(vm.uiState.value.recommending)

        val id = history.observeConversations().first().single().id
        assertEquals(listOf(place), history.messages(id).last().places)
    }

    @Test fun `recommendation failure shows an error and retry asks again`() = runTest {
        repo.recommendation = Outcome.Failure(AppError.Ai(AiFailure.UNAVAILABLE))
        val vm = vm(mapOf("destinationId" to "jaipur"))
        vm.onAction(AssistantAction.Recommend(PlaceKind.STAY))
        assertEquals(AppError.Ai(AiFailure.UNAVAILABLE), vm.uiState.value.error)
        assertEquals(1, vm.uiState.value.messages.size)

        repo.recommendation = Outcome.Success(listOf(place.copy(kind = PlaceKind.STAY)))
        vm.onAction(AssistantAction.Retry)
        assertNull(vm.uiState.value.error)
        assertEquals(2, repo.recommendRequests.size)
        assertEquals(1, vm.uiState.value.messages.last().places.size)
        assertEquals(0, repo.requests.size) // no chat call for structured results
    }

    @Test fun `stays without a destination fall back to a chat question`() = runTest {
        val vm = vm()
        vm.onAction(AssistantAction.Recommend(PlaceKind.STAY))
        assertEquals(0, repo.recommendRequests.size)
        assertEquals(1, repo.requests.size)
    }

    @Test fun `chat failure removes empty bubble, retry re-sends the last question`() = runTest {
        repo.events = listOf(AssistantEvent.Failed(AppError.Ai(AiFailure.UNAVAILABLE)))
        val vm = vm()
        vm.onAction(AssistantAction.SendSuggestion("Hi"))
        assertEquals(listOf(ChatRole.USER), vm.uiState.value.messages.map { it.role })
        assertEquals(AppError.Ai(AiFailure.UNAVAILABLE), vm.uiState.value.error)

        repo.events = listOf(AssistantEvent.Delta("Back!"), AssistantEvent.Done)
        vm.onAction(AssistantAction.Retry)
        assertEquals(listOf(ChatRole.USER to "Hi", ChatRole.ASSISTANT to "Back!"), vm.uiState.value.messages.map { it.role to it.text })
        assertEquals(2, repo.requests.size)
        assertNull(vm.uiState.value.error)
    }

    @Test fun `resuming a conversation restores messages, context and preferences`() = runTest {
        val prefs = TripPreferences(BudgetLevel.LUXURY, days = 2)
        val id = history.create("Old chat", "jaipur", null, prefs)
        history.save(id, ChatMessage("m1", ChatRole.USER, "Hello", clock.instant()))
        val vm = vm(mapOf("conversationId" to id))
        val s = vm.uiState.value
        assertEquals("Jaipur", s.destinationName)
        assertEquals(prefs, s.preferences)
        assertEquals(listOf("Hello"), s.messages.map { it.text })

        vm.onAction(AssistantAction.SendSuggestion("More?"))
        assertEquals(1, history.observeConversations().first().size) // continues the same conversation
        assertEquals(BudgetLevel.LUXURY, repo.requests.single().second.preferences.budgetLevel)
    }

    @Test fun `preferences are saved and new chat starts a separate conversation`() = runTest {
        val vm = vm(mapOf("destinationId" to "jaipur"))
        vm.onAction(AssistantAction.SendSuggestion("Hi"))
        val first = history.observeConversations().first().single().id
        vm.onAction(AssistantAction.UpdatePreferences(TripPreferences(BudgetLevel.BUDGET)))
        assertEquals(BudgetLevel.BUDGET, history.conversation(first)?.preferences?.budgetLevel)

        vm.onAction(AssistantAction.NewChat)
        assertTrue(vm.uiState.value.messages.isEmpty())
        assertEquals("Jaipur", vm.uiState.value.destinationName) // context is kept
        vm.onAction(AssistantAction.SendSuggestion("Again"))
        assertEquals(2, history.observeConversations().first().size)
    }

    @Test fun `blank input is not sent`() = runTest {
        val vm = vm()
        vm.onAction(AssistantAction.InputChanged("   "))
        vm.onAction(AssistantAction.Send)
        assertEquals(0, repo.requests.size)
        assertTrue(history.observeConversations().first().isEmpty())
    }
}
