package com.tripwise.core.domain.usecase

import com.tripwise.core.domain.repository.SavedDestinationRepository
import com.tripwise.core.model.Destination
import com.tripwise.core.model.SaveableDestination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Joins any destination list with the user's saved ids so cards can show the heart state. */
class ObserveSaveableDestinationsUseCase @Inject constructor(
    private val savedRepository: SavedDestinationRepository,
) {
    operator fun invoke(destinations: Flow<List<Destination>>): Flow<List<SaveableDestination>> =
        combine(destinations, savedRepository.observeSavedIds()) { list, saved ->
            list.map { SaveableDestination(it, it.id in saved) }
        }
}
