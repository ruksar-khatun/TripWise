package com.tripwise.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.tripwise.core.database.entity.BudgetAllocationEntity
import com.tripwise.core.database.entity.CategoryTotal
import com.tripwise.core.database.entity.ExpenseEntity
import com.tripwise.core.database.entity.ItineraryItemEntity
import com.tripwise.core.database.entity.SavedDestinationEntity
import com.tripwise.core.database.entity.TripEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface TripDao {
    @Query("SELECT * FROM trips WHERE deleted = 0 ORDER BY start_date ASC")
    fun observeAll(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :id AND deleted = 0")
    fun observeById(id: String): Flow<TripEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(trip: TripEntity)

    @Query("UPDATE trips SET budget_minor = :minor, budget_currency = :currency, sync_state = 'PENDING', updated_at = :now WHERE id = :id")
    suspend fun updateBudget(id: String, minor: Long?, currency: String?, now: Instant)

    /** Soft delete; the sync phase uploads the deletion and then purges the row. */
    @Query("UPDATE trips SET deleted = 1, sync_state = 'PENDING', updated_at = :now WHERE id = :id")
    suspend fun markDeleted(id: String, now: Instant)
}

@Dao
interface ItineraryDao {
    @Query("SELECT * FROM itinerary_items WHERE trip_id = :tripId AND deleted = 0 ORDER BY date ASC, start_time ASC")
    fun observeForTrip(tripId: String): Flow<List<ItineraryItemEntity>>

    @Upsert
    suspend fun upsert(items: List<ItineraryItemEntity>)

    @Query("UPDATE itinerary_items SET is_done = :done, sync_state = 'PENDING', updated_at = :now WHERE id = :id")
    suspend fun setDone(id: String, done: Boolean, now: Instant)

    @Query("UPDATE itinerary_items SET deleted = 1, sync_state = 'PENDING', updated_at = :now WHERE id = :id")
    suspend fun markDeleted(id: String, now: Instant)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_allocations WHERE trip_id = :tripId")
    fun observeAllocations(tripId: String): Flow<List<BudgetAllocationEntity>>

    @Upsert
    suspend fun upsertAllocations(items: List<BudgetAllocationEntity>)

    @Query("DELETE FROM budget_allocations WHERE trip_id = :tripId")
    suspend fun clearAllocations(tripId: String)

    @Query(
        """SELECT category, SUM(amount_minor) AS total_minor FROM expenses
           WHERE trip_id = :tripId AND deleted = 0 GROUP BY category""",
    )
    fun observeSpentByCategory(tripId: String): Flow<List<CategoryTotal>>
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE trip_id = :tripId AND deleted = 0 ORDER BY spent_on DESC, updated_at DESC")
    fun observeForTrip(tripId: String): Flow<List<ExpenseEntity>>

    @Upsert
    suspend fun upsert(expense: ExpenseEntity)

    @Query("UPDATE expenses SET deleted = 1, sync_state = 'PENDING', updated_at = :now WHERE id = :id")
    suspend fun markDeleted(id: String, now: Instant)
}

@Dao
interface SavedDestinationDao {
    @Query("SELECT * FROM saved_destinations WHERE deleted = 0 ORDER BY saved_at DESC")
    fun observeAll(): Flow<List<SavedDestinationEntity>>

    @Upsert
    suspend fun upsert(item: SavedDestinationEntity)

    @Query("UPDATE saved_destinations SET deleted = 1, sync_state = 'PENDING' WHERE destination_id = :id")
    suspend fun markDeleted(id: String)
}
