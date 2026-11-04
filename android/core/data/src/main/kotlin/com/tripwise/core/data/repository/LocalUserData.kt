package com.tripwise.core.data.repository

import com.tripwise.core.database.DatabaseWiper
import javax.inject.Inject

/** Everything stored on-device for the signed-in user; wiped on sign-out and account deletion. */
interface LocalUserData {
    suspend fun clear()
}

internal class RoomLocalUserData @Inject constructor(private val wiper: DatabaseWiper) : LocalUserData {
    override suspend fun clear() = wiper.wipe()
}
