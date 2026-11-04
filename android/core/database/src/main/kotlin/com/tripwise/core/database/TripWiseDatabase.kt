package com.tripwise.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tripwise.core.database.converter.TimeConverters
import com.tripwise.core.database.dao.BudgetDao
import com.tripwise.core.database.dao.ItineraryDao
import com.tripwise.core.database.dao.SavedDestinationDao
import com.tripwise.core.database.dao.TripDao
import com.tripwise.core.database.entity.BudgetAllocationEntity
import com.tripwise.core.database.entity.ExpenseEntity
import com.tripwise.core.database.entity.ItineraryItemEntity
import com.tripwise.core.database.entity.SavedDestinationEntity
import com.tripwise.core.database.entity.TripEntity

/**
 * Single source of truth for the signed-in user's data on this device.
 * Schema changes: bump [version], add a Migration (or AutoMigration) and commit the exported
 * schema JSON in core/database/schemas. Never use destructive migration for user data.
 */
@Database(
    entities = [
        TripEntity::class,
        ItineraryItemEntity::class,
        BudgetAllocationEntity::class,
        ExpenseEntity::class,
        SavedDestinationEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(TimeConverters::class)
abstract class TripWiseDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun itineraryDao(): ItineraryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savedDestinationDao(): SavedDestinationDao
}
