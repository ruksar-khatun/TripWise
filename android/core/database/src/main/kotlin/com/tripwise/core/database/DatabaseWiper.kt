package com.tripwise.core.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Deletes every row of the signed-in user's local data (sign-out / account deletion).
 * Exposes no Room types, so modules using it don't need Room on their classpath.
 */
class DatabaseWiper @Inject internal constructor(private val database: TripWiseDatabase) {
    suspend fun wipe() = withContext(Dispatchers.IO) { database.clearAllTables() }
}
