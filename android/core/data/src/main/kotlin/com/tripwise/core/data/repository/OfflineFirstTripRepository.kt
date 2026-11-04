package com.tripwise.core.data.repository

import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.common.result.outcomeOf
import com.tripwise.core.data.mapper.enumOr
import com.tripwise.core.data.mapper.toModel
import com.tripwise.core.database.dao.BudgetDao
import com.tripwise.core.database.dao.ItineraryDao
import com.tripwise.core.database.dao.TripDao
import com.tripwise.core.database.entity.ItineraryItemEntity
import com.tripwise.core.database.entity.TripEntity
import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.domain.repository.TripRepository
import com.tripwise.core.model.BudgetSummary
import com.tripwise.core.model.CategoryBudget
import com.tripwise.core.model.ExpenseCategory
import com.tripwise.core.model.ItineraryItem
import com.tripwise.core.model.Money
import com.tripwise.core.model.NewTrip
import com.tripwise.core.model.Trip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

/** Room is the source of truth. Writes are marked PENDING for the sync phase to upload. */
internal class OfflineFirstTripRepository @Inject constructor(
    private val tripDao: TripDao,
    private val itineraryDao: ItineraryDao,
    private val budgetDao: BudgetDao,
    private val destinations: DestinationRepository,
    private val clock: Clock,
) : TripRepository {

    override fun observeTrips(): Flow<List<Trip>> = tripDao.observeAll().map { list -> list.map { it.toModel() } }

    override fun observeTrip(id: String): Flow<Trip?> = tripDao.observeById(id).map { it?.toModel() }

    override fun observeItinerary(tripId: String): Flow<List<ItineraryItem>> =
        itineraryDao.observeForTrip(tripId).map { list -> list.map { it.toModel() } }

    override fun observeBudget(tripId: String): Flow<BudgetSummary?> = combine(
        tripDao.observeById(tripId),
        budgetDao.observeAllocations(tripId),
        budgetDao.observeSpentByCategory(tripId),
    ) { trip, allocations, spent ->
        val total = trip?.toModel()?.budget ?: return@combine null
        val currency = total.currency
        val planned = allocations.associate { it.category to it.plannedMinor }
        val spentBy = spent.associate { it.category to it.totalMinor }
        val categories = (planned.keys + spentBy.keys).map { key ->
            CategoryBudget(
                category = enumOr(key, ExpenseCategory.OTHER),
                planned = Money(planned[key] ?: 0, currency),
                spent = Money(spentBy[key] ?: 0, currency),
            )
        }.sortedBy { it.category.ordinal }
        BudgetSummary(tripId, total, categories)
    }

    override suspend fun setItineraryItemDone(itemId: String, done: Boolean) =
        itineraryDao.setDone(itemId, done, Instant.now(clock))

    override suspend fun createTrip(trip: NewTrip): Outcome<String> = outcomeOf({ AppError.Unknown(it) }) {
        // Link to the catalog when possible (by id, else by exact name) for the cover photo.
        val catalog = trip.destinationId?.let { destinations.observeDestination(it).first() }
            ?: destinations.observeDestinations(trip.destinationName).first()
                .firstOrNull { it.name.equals(trip.destinationName.trim(), ignoreCase = true) }
        val now = Instant.now(clock)
        val id = UUID.randomUUID().toString()
        tripDao.insert(
            TripEntity(
                id = id,
                title = trip.title.trim(),
                destinationId = catalog?.id,
                destinationName = catalog?.name ?: trip.destinationName.trim(),
                coverImageUrl = catalog?.coverImage?.url,
                startDate = trip.startDate,
                endDate = trip.endDate,
                travellers = trip.travellers,
                budgetMinor = trip.budget?.amountMinor,
                budgetCurrency = trip.budget?.currency?.code,
                notes = trip.notes?.trim()?.ifEmpty { null },
                createdAt = now,
                updatedAt = now,
            ),
        )
        id
    }

    override suspend fun deleteTrip(id: String) = tripDao.markDeleted(id, Instant.now(clock))

    override suspend fun saveItineraryItem(item: ItineraryItem) = addItineraryItems(item.tripId, listOf(item))

    override suspend fun deleteItineraryItem(itemId: String) = itineraryDao.markDeleted(itemId, Instant.now(clock))

    override suspend fun addItineraryItems(tripId: String, items: List<ItineraryItem>) {
        val now = Instant.now(clock)
        itineraryDao.upsert(
            items.map {
                ItineraryItemEntity(
                    id = it.id, tripId = tripId, date = it.date, startTime = it.startTime, title = it.title.take(100),
                    category = it.category.name, costMinor = it.estimatedCost?.amountMinor, costCurrency = it.estimatedCost?.currency?.code,
                    isDone = it.isDone, notes = it.notes, updatedAt = now,
                )
            },
        )
    }
}
