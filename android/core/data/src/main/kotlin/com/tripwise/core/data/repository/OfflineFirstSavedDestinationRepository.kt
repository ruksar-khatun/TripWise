package com.tripwise.core.data.repository

import com.tripwise.core.database.dao.SavedDestinationDao
import com.tripwise.core.database.entity.SavedDestinationEntity
import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.domain.repository.SavedDestinationRepository
import com.tripwise.core.model.SavedDestination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

internal class OfflineFirstSavedDestinationRepository @Inject constructor(
    private val dao: SavedDestinationDao,
    private val destinations: DestinationRepository,
    private val clock: Clock,
) : SavedDestinationRepository {

    override fun observeSavedIds(): Flow<Set<String>> = dao.observeAll().map { rows -> rows.map { it.destinationId }.toSet() }

    override fun observeSaved(): Flow<List<SavedDestination>> =
        combine(dao.observeAll(), destinations.observeDestinations()) { rows, catalog ->
            val byId = catalog.associateBy { it.id }
            // Rows whose destination left the catalog are skipped rather than shown broken.
            rows.mapNotNull { row -> byId[row.destinationId]?.let { SavedDestination(it, row.savedAt) } }
        }

    override suspend fun setSaved(destinationId: String, saved: Boolean) {
        if (saved) dao.upsert(SavedDestinationEntity(destinationId, Instant.now(clock))) else dao.markDeleted(destinationId)
    }
}
