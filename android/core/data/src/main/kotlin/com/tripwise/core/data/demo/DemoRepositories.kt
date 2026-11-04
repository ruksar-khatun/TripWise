package com.tripwise.core.data.demo

import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.model.Destination
import com.tripwise.core.model.TravelInterest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

/*
 * DEMO catalog repository: in-memory destinations behind the real interface, with a short artificial
 * delay so loading states are visible. Replaced by the backend catalog in the catalog phase.
 */

private const val DEMO_LATENCY_MS = 300L

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
