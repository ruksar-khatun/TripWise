package com.tripwise.core.data.demo

import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.domain.repository.SavedDestinationRepository
import com.tripwise.core.domain.repository.TripRepository
import com.tripwise.core.domain.repository.UserRepository
import com.tripwise.core.model.BudgetSummary
import com.tripwise.core.model.Destination
import com.tripwise.core.model.ItineraryItem
import com.tripwise.core.model.SavedDestination
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.model.Trip
import com.tripwise.core.model.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/*
 * DEMO repositories for the UI phase: in-memory, behind the real domain interfaces.
 * A short artificial delay makes loading states visible. Swapped for Room/network
 * implementations in later phases without changing any ViewModel or screen.
 */

private const val DEMO_LATENCY_MS = 450L

private fun <T> Flow<T>.withDemoLatency(): Flow<T> = onStart { delay(DEMO_LATENCY_MS) }

@Singleton
internal class DemoDestinationRepository @Inject constructor() : DestinationRepository {
    private val all = DemoContent.destinations

    override fun observeDestinations(query: String, interest: TravelInterest?): Flow<List<Destination>> {
        val q = query.trim()
        return flowOf(
            all.filter { d ->
                (q.isEmpty() || d.name.contains(q, ignoreCase = true) || d.country.contains(q, ignoreCase = true)) &&
                    (interest == null || interest in d.interests)
            },
        ).withDemoLatency()
    }

    override fun observePopular(limit: Int): Flow<List<Destination>> =
        flowOf(all.sortedByDescending { it.ratingCount ?: 0 }.take(limit)).withDemoLatency()

    override fun observeDestination(id: String): Flow<Destination?> =
        flowOf(all.firstOrNull { it.id == id }).withDemoLatency()
}

@Singleton
internal class DemoSavedDestinationRepository @Inject constructor(clock: Clock) : SavedDestinationRepository {
    private val saved = MutableStateFlow(
        DemoContent.initiallySaved.mapIndexed { i, id -> id to Instant.now(clock).minusSeconds(3_600L * (i + 1)) }.toMap(),
    )
    private val byId = DemoContent.destinations.associateBy { it.id }

    override fun observeSavedIds(): Flow<Set<String>> = saved.map { it.keys }

    override fun observeSaved(): Flow<List<SavedDestination>> = saved
        .map { m -> m.mapNotNull { (id, at) -> byId[id]?.let { SavedDestination(it, at) } }.sortedByDescending { it.savedAt } }
        .withDemoLatency()

    override suspend fun setSaved(destinationId: String, saved: Boolean) {
        this.saved.update { if (saved) it + (destinationId to Instant.now()) else it - destinationId }
    }
}

@Singleton
internal class DemoTripRepository @Inject constructor(clock: Clock) : TripRepository {
    private val today = LocalDate.now(clock)
    private val trips = DemoContent.trips(today)
    private val items = MutableStateFlow(DemoContent.itinerary(today))

    override fun observeTrips(): Flow<List<Trip>> = flowOf(trips.sortedBy { it.startDate }).withDemoLatency()

    override fun observeTrip(id: String): Flow<Trip?> = flowOf(trips.firstOrNull { it.id == id }).withDemoLatency()

    override fun observeItinerary(tripId: String): Flow<List<ItineraryItem>> = items
        .map { list -> list.filter { it.tripId == tripId }.sortedWith(compareBy({ it.date }, { it.startTime })) }
        .withDemoLatency()

    override fun observeBudget(tripId: String): Flow<BudgetSummary?> =
        flowOf(DemoContent.budgets.firstOrNull { it.tripId == tripId }).withDemoLatency()

    override suspend fun setItineraryItemDone(itemId: String, done: Boolean) {
        items.update { list -> list.map { if (it.id == itemId) it.copy(isDone = done) else it } }
    }
}

@Singleton
internal class DemoUserRepository @Inject constructor() : UserRepository {
    override fun observeProfile(): Flow<UserProfile> = flowOf(DemoContent.profile).withDemoLatency()
}
