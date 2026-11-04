package com.tripwise.core.testing.fakes

import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.domain.repository.SavedDestinationRepository
import com.tripwise.core.domain.repository.TripRepository
import com.tripwise.core.domain.repository.UserRepository
import com.tripwise.core.model.BudgetSummary
import com.tripwise.core.model.Destination
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.model.ItineraryItem
import com.tripwise.core.model.NewTrip
import com.tripwise.core.model.SavedDestination
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.model.Trip
import com.tripwise.core.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Instant

/** Fakes hold state in MutableStateFlows; a null value means "still loading" (nothing emitted). */
class FakeDestinationRepository(initial: List<Destination>? = emptyList()) : DestinationRepository {
    val destinations = MutableStateFlow(initial)
    val queries = mutableListOf<Pair<String, TravelInterest?>>()

    override fun observeDestinations(query: String, interest: TravelInterest?): Flow<List<Destination>> {
        queries += query to interest
        return destinations.filterNotNull().map { all ->
            all.filter {
                (query.isBlank() || it.name.contains(query.trim(), true) || it.country.contains(query.trim(), true)) &&
                    (interest == null || interest in it.interests)
            }
        }
    }

    override fun observePopular(limit: Int) = destinations.filterNotNull().map { it.take(limit) }

    override fun observeFeatured() = destinations.filterNotNull().map { l -> l.filter { it.featured } }

    override fun observeDestination(id: String) = destinations.filterNotNull().map { l -> l.firstOrNull { it.id == id } }
}

class FakeSavedDestinationRepository(
    private val catalog: List<Destination> = emptyList(),
    initiallySaved: Map<String, Instant> = emptyMap(),
) : SavedDestinationRepository {
    val saved = MutableStateFlow(initiallySaved)

    override fun observeSavedIds(): Flow<Set<String>> = saved.map { it.keys }

    override fun observeSaved(): Flow<List<SavedDestination>> = saved.map { m ->
        m.mapNotNull { (id, at) -> catalog.firstOrNull { it.id == id }?.let { SavedDestination(it, at) } }
            .sortedByDescending { it.savedAt }
    }

    override suspend fun setSaved(destinationId: String, saved: Boolean) {
        this.saved.update { if (saved) it + (destinationId to Instant.now()) else it - destinationId }
    }
}

class FakeTripRepository(
    trips: List<Trip>? = emptyList(),
    items: List<ItineraryItem> = emptyList(),
    budgets: List<BudgetSummary> = emptyList(),
) : TripRepository {
    val trips = MutableStateFlow(trips)
    val items = MutableStateFlow(items)
    val budgets = MutableStateFlow(budgets)

    override fun observeTrips() = trips.filterNotNull().map { l -> l.sortedBy { it.startDate } }
    override fun observeTrip(id: String) = trips.filterNotNull().map { l -> l.firstOrNull { it.id == id } }
    override fun observeItinerary(tripId: String) = items.map { l -> l.filter { it.tripId == tripId } }
    override fun observeBudget(tripId: String) = budgets.map { l -> l.firstOrNull { it.tripId == tripId } }
    override suspend fun setItineraryItemDone(itemId: String, done: Boolean) {
        items.update { l -> l.map { if (it.id == itemId) it.copy(isDone = done) else it } }
    }

    val created = mutableListOf<NewTrip>()
    var createResult: Outcome<String>? = null

    override suspend fun createTrip(trip: NewTrip): Outcome<String> {
        created += trip
        createResult?.let { return it }
        val id = "trip-${created.size}"
        trips.update { (it ?: emptyList()) + Trip(id, trip.title, trip.destinationId, trip.destinationName, null,
            trip.startDate, trip.endDate, trip.travellers, trip.budget, trip.notes) }
        return Outcome.Success(id)
    }

    override suspend fun deleteTrip(id: String) {
        trips.update { l -> l?.filterNot { it.id == id } }
    }

    override suspend fun addItineraryItems(tripId: String, items: List<ItineraryItem>) {
        this.items.update { it + items }
    }

    override suspend fun saveItineraryItem(item: ItineraryItem) {
        items.update { l -> l.filterNot { it.id == item.id } + item }
    }

    override suspend fun deleteItineraryItem(itemId: String) {
        items.update { l -> l.filterNot { it.id == itemId } }
    }
}

class FakeUserRepository(profile: UserProfile? = null) : UserRepository {
    val profile = MutableStateFlow(profile)
    override fun observeProfile(): Flow<UserProfile> = profile.filterNotNull()
}

class FakePlacesRepository(var result: com.tripwise.core.common.result.Outcome<List<com.tripwise.core.model.NearbyPlace>> =
    com.tripwise.core.common.result.Outcome.Success(emptyList())) : com.tripwise.core.domain.repository.PlacesRepository {
    var calls = 0
    override suspend fun nearby(center: com.tripwise.core.model.GeoPoint, radiusMeters: Int) = result.also { calls++ }
}

class FakeAssistantRepository : com.tripwise.core.domain.repository.AssistantRepository {
    var events: List<com.tripwise.core.domain.repository.AssistantEvent> =
        listOf(com.tripwise.core.domain.repository.AssistantEvent.Delta("Hi"), com.tripwise.core.domain.repository.AssistantEvent.Done)
    val requests = mutableListOf<Pair<List<com.tripwise.core.model.ChatMessage>, com.tripwise.core.model.AssistantContext>>()
    override fun reply(history: List<com.tripwise.core.model.ChatMessage>, context: com.tripwise.core.model.AssistantContext) =
        kotlinx.coroutines.flow.flow { requests += history to context; events.forEach { emit(it) } }
}

class FakeItineraryPlanRepository(var result: com.tripwise.core.common.result.Outcome<com.tripwise.core.model.ItineraryPlan>? = null) :
    com.tripwise.core.domain.repository.ItineraryPlanRepository {
    val requests = mutableListOf<com.tripwise.core.model.ItineraryRequest>()
    var gate: kotlinx.coroutines.CompletableDeferred<Unit>? = null
    override suspend fun generate(request: com.tripwise.core.model.ItineraryRequest): com.tripwise.core.common.result.Outcome<com.tripwise.core.model.ItineraryPlan> {
        requests += request
        gate?.await()
        return checkNotNull(result) { "set FakeItineraryPlanRepository.result" }
    }
}

class FakeExpenseRepository(private val trips: FakeTripRepository? = null) : com.tripwise.core.domain.repository.ExpenseRepository {
    val expenses = MutableStateFlow<List<com.tripwise.core.model.Expense>>(emptyList())
    val plans = mutableMapOf<String, Map<com.tripwise.core.model.ExpenseCategory, com.tripwise.core.model.Money>>()
    override fun observeExpenses(tripId: String) = expenses.map { l -> l.filter { it.tripId == tripId } }
    override suspend fun saveExpense(tripId: String, expenseId: String?, draft: com.tripwise.core.model.ExpenseDraft): Outcome<Unit> {
        val id = expenseId ?: "e${expenses.value.size + 1}"
        expenses.update { l -> l.filterNot { it.id == id } + com.tripwise.core.model.Expense(id, tripId, draft.category, draft.amount, draft.note, draft.spentOn) }
        return Outcome.Success(Unit)
    }
    override suspend fun deleteExpense(expenseId: String) { expenses.update { l -> l.filterNot { it.id == expenseId } } }
    override suspend fun setPlan(tripId: String, planned: Map<com.tripwise.core.model.ExpenseCategory, com.tripwise.core.model.Money>) { plans[tripId] = planned }
    override suspend fun setPlanned(tripId: String, category: com.tripwise.core.model.ExpenseCategory, amount: com.tripwise.core.model.Money) {
        plans[tripId] = (plans[tripId] ?: emptyMap()) + (category to amount)
    }
}

class FakeCurrencyRepository(var result: Outcome<com.tripwise.core.model.ExchangeRates> =
    Outcome.Success(com.tripwise.core.model.ExchangeRates(mapOf("USD" to 1.0, "INR" to 80.0, "JPY" to 150.0), live = true))) :
    com.tripwise.core.domain.repository.CurrencyRepository {
    override suspend fun usdRates() = result
}
