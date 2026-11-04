package com.tripwise.core.domain.repository

import com.tripwise.core.model.BudgetSummary
import com.tripwise.core.model.ItineraryItem
import com.tripwise.core.model.Trip
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    /** All trips, ordered by start date. */
    fun observeTrips(): Flow<List<Trip>>

    fun observeTrip(id: String): Flow<Trip?>

    /** Items ordered by date then start time. */
    fun observeItinerary(tripId: String): Flow<List<ItineraryItem>>

    fun observeBudget(tripId: String): Flow<BudgetSummary?>

    suspend fun setItineraryItemDone(itemId: String, done: Boolean)
}
