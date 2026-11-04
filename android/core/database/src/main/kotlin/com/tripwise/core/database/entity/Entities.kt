package com.tripwise.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "trips", indices = [Index("start_date")])
data class TripEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "destination_id") val destinationId: String?,
    @ColumnInfo(name = "destination_name") val destinationName: String,
    @ColumnInfo(name = "cover_image_url") val coverImageUrl: String?,
    @ColumnInfo(name = "start_date") val startDate: LocalDate,
    @ColumnInfo(name = "end_date") val endDate: LocalDate,
    val travellers: Int,
    /** Money as minor units + ISO currency; both null when no budget. */
    @ColumnInfo(name = "budget_minor") val budgetMinor: Long?,
    @ColumnInfo(name = "budget_currency") val budgetCurrency: String?,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "sync_state") val syncState: SyncState = SyncState.PENDING,
    @ColumnInfo(name = "server_version") val serverVersion: Long? = null,
    val deleted: Boolean = false,
)

@Entity(
    tableName = "itinerary_items",
    foreignKeys = [ForeignKey(TripEntity::class, ["id"], ["trip_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("trip_id")],
)
data class ItineraryItemEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "trip_id") val tripId: String,
    val date: LocalDate,
    @ColumnInfo(name = "start_time") val startTime: LocalTime?,
    val title: String,
    val category: String,
    @ColumnInfo(name = "cost_minor") val costMinor: Long?,
    @ColumnInfo(name = "cost_currency") val costCurrency: String?,
    @ColumnInfo(name = "is_done") val isDone: Boolean,
    val notes: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "sync_state") val syncState: SyncState = SyncState.PENDING,
    val deleted: Boolean = false,
)

/** Planned amount per expense category for a trip. */
@Entity(
    tableName = "budget_allocations",
    primaryKeys = ["trip_id", "category"],
    foreignKeys = [ForeignKey(TripEntity::class, ["id"], ["trip_id"], onDelete = ForeignKey.CASCADE)],
)
data class BudgetAllocationEntity(
    @ColumnInfo(name = "trip_id") val tripId: String,
    val category: String,
    @ColumnInfo(name = "planned_minor") val plannedMinor: Long,
    val currency: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "sync_state") val syncState: SyncState = SyncState.PENDING,
)

@Entity(
    tableName = "expenses",
    foreignKeys = [ForeignKey(TripEntity::class, ["id"], ["trip_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("trip_id")],
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "trip_id") val tripId: String,
    val category: String,
    @ColumnInfo(name = "amount_minor") val amountMinor: Long,
    val currency: String,
    val note: String?,
    @ColumnInfo(name = "spent_on") val spentOn: LocalDate,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "sync_state") val syncState: SyncState = SyncState.PENDING,
    val deleted: Boolean = false,
)

@Entity(tableName = "saved_destinations")
data class SavedDestinationEntity(
    @PrimaryKey @ColumnInfo(name = "destination_id") val destinationId: String,
    @ColumnInfo(name = "saved_at") val savedAt: Instant,
    @ColumnInfo(name = "sync_state") val syncState: SyncState = SyncState.PENDING,
    val deleted: Boolean = false,
)

/** Result row for spent-per-category aggregation. */
data class CategoryTotal(val category: String, @ColumnInfo(name = "total_minor") val totalMinor: Long)
