package com.tripwise.feature.assistant

import androidx.lifecycle.SavedStateHandle
import com.tripwise.core.common.error.AiFailure
import com.tripwise.core.common.error.AppError
import com.tripwise.core.domain.repository.AssistantEvent
import com.tripwise.core.model.ChatRole
import com.tripwise.core.testing.data.TestData
import com.tripwise.core.testing.fakes.FakeAssistantRepository
import com.tripwise.core.testing.fakes.FakeDestinationRepository
import com.tripwise.core.testing.fakes.FakeTripRepository
import com.tripwise.core.testing.rules.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class AssistantViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val kyoto = TestData.destination("kyoto", name = "Kyoto")
    private val repo = FakeAssistantRepository()

    private fun vm(args: Map<String, Any?> = emptyMap()) =
        AssistantViewModel(SavedStateHandle(args), repo, FakeTripRepository(listOf(TestData.trip("t1", LocalDate.of(2026, 4, 1), destinationId = "kyoto"))), FakeDestinationRepository(listOf(kyoto)))

    @Test fun `streams a reply into one assistant bubble`() = runTest {
        repo.events = listOf(AssistantEvent.Delta("Hel"), AssistantEvent.Delta("lo"), AssistantEvent.Done)
        val vm = vm()
        vm.onAction(AssistantAction.InputChanged("Hi"))
        vm.onAction(AssistantAction.Send)
        val s = vm.uiState.value
        assertEquals(listOf(ChatRole.USER to "Hi", ChatRole.ASSISTANT to "Hello"), s.messages.map { it.role to it.text })
        assertEquals("", s.input)
        assertFalse(s.sending)
    }

    @Test fun `trip context resolves title and destination`() = runTest {
        val vm = vm(mapOf("tripId" to "t1"))
        assertEquals("Trip t1", vm.uiState.value.contextTitle)
        vm.onAction(AssistantAction.SendSuggestion("Budget per day?"))
        val (_, ctx) = repo.requests.single()
        assertEquals("kyoto", ctx.destinationId)
        assertEquals("t1", ctx.trip?.id)
    }

    @Test fun `failure removes empty bubble, retry re-sends the last question`() = runTest {
        repo.events = listOf(AssistantEvent.Failed(AppError.Ai(AiFailure.UNAVAILABLE)))
        val vm = vm()
        vm.onAction(AssistantAction.SendSuggestion("Hi"))
        assertEquals(listOf(ChatRole.USER), vm.uiState.value.messages.map { it.role })
        assertEquals(AppError.Ai(AiFailure.UNAVAILABLE), vm.uiState.value.error)

        repo.events = listOf(AssistantEvent.Delta("Back!"), AssistantEvent.Done)
        vm.onAction(AssistantAction.Retry)
        assertEquals(listOf(ChatRole.USER to "Hi", ChatRole.ASSISTANT to "Back!"), vm.uiState.value.messages.map { it.role to it.text })
        assertEquals(2, repo.requests.size)
        assertEquals(null, vm.uiState.value.error)
    }

    @Test fun `blank input is not sent`() = runTest {
        val vm = vm()
        vm.onAction(AssistantAction.InputChanged("   "))
        vm.onAction(AssistantAction.Send)
        assertEquals(0, repo.requests.size)
    }
}
