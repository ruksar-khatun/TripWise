package com.tripwise.core.domain.repository

import com.tripwise.core.model.Destination
import com.tripwise.core.model.TravelInterest
import kotlinx.coroutines.flow.Flow

interface DestinationRepository {
    /** Destinations matching [query] (name/country, case-insensitive) and, if set, [interest]. */
    fun observeDestinations(query: String = "", interest: TravelInterest? = null): Flow<List<Destination>>

    fun observePopular(limit: Int = 8): Flow<List<Destination>>

    /** Emits null when no destination has this id. */
    fun observeDestination(id: String): Flow<Destination?>
}
