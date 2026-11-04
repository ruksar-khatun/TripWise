package com.tripwise.core.data.repository

import com.tripwise.core.common.coroutines.Dispatcher
import com.tripwise.core.common.coroutines.TripWiseDispatcher
import com.tripwise.core.database.TripWiseDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Everything stored on-device for the signed-in user; wiped on sign-out and account deletion. */
interface LocalUserData {
    suspend fun clear()
}

internal class RoomLocalUserData @Inject constructor(
    private val database: TripWiseDatabase,
    @Dispatcher(TripWiseDispatcher.IO) private val io: CoroutineDispatcher,
) : LocalUserData {
    override suspend fun clear() = withContext(io) { database.clearAllTables() }
}
