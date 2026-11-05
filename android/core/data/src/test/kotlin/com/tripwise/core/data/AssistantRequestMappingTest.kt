package com.tripwise.core.data

import com.tripwise.core.data.repository.DefaultAssistantRepository
import com.tripwise.core.model.ActivityCategory
import com.tripwise.core.model.AssistantContext
import com.tripwise.core.model.BudgetLevel
import com.tripwise.core.model.ChatMessage
import com.tripwise.core.model.ChatRole
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.ItineraryItem
import com.tripwise.core.model.Money
import com.tripwise.core.model.PlaceKind
import com.tripwise.core.model.PlaceSuggestion
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.model.Trip
import com.tripwise.core.model.TripPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class AssistantRequestMappingTest {
    private val trip = Trip("t", "Goa break", "goa", "Goa", null, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 4), 2,
        Money(40_000_00, CurrencyCode("INR")), null)

    @Test fun `history starts at a user turn, keeps place cards as text and drops empty bubbles`() {
        val history = listOf(
            ChatMessage("0", ChatRole.ASSISTANT, "Welcome"),
            ChatMessage("1", ChatRole.USER, "Where to stay?"),
            ChatMessage("2", ChatRole.ASSISTANT, "", places = listOf(PlaceSuggestion("Taj Fort Aguada", PlaceKind.STAY, "Candolim", "Resort", "₹15,000+", "Sea views"))),
            ChatMessage("3", ChatRole.ASSISTANT, ""),
            ChatMessage("4", ChatRole.USER, "Cheaper?"),
        )
        val dto = DefaultAssistantRepository.buildChatRequest(history, AssistantContext())
        assertEquals(listOf("user", "assistant", "user"), dto.messages.map { it.role })
        assertEquals("- Taj Fort Aguada (Candolim, ₹15,000+)", dto.messages[1].text)
        assertNull(dto.context?.preferences)
        assertNull(dto.context?.trip)
    }

    @Test fun `trip budget, planned items and preferences are sent`() {
        val item = ItineraryItem("i", "t", LocalDate.of(2026, 12, 1), LocalTime.of(10, 0), "Baga Beach", ActivityCategory.SIGHTSEEING, null, false)
        val prefs = TripPreferences(BudgetLevel.BUDGET, days = 4, travellers = 2, interests = setOf(TravelInterest.FOOD, TravelInterest.BEACHES))
        val dto = DefaultAssistantRepository.buildChatRequest(
            listOf(ChatMessage("1", ChatRole.USER, "Plan it")),
            AssistantContext(destinationId = "goa", trip = trip, itinerary = listOf(item), preferences = prefs),
        )
        val ctx = dto.context!!
        assertEquals("goa", ctx.destinationId)
        assertEquals(40_000L, ctx.trip?.budgetInr)
        assertEquals(listOf("2026-12-01 10:00 Baga Beach"), ctx.trip?.itinerary)
        assertEquals("BUDGET", ctx.preferences?.budgetLevel)
        assertEquals(listOf("BEACHES", "FOOD"), ctx.preferences?.interests)
    }
}
