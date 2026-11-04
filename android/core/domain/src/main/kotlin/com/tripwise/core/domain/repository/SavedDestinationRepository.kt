package com.tripwise.core.domain.repository

import com.tripwise.core.model.SavedDestination
import kotlinx.coroutines.flow.Flow

interface SavedDestinationRepository {
    fun observeSavedIds(): Flow<Set<String>>

    /** Most recently saved first. */
    fun observeSaved(): Flow<List<SavedDestination>>

    suspend fun setSaved(destinationId: String, saved: Boolean)
}
