package com.tripwise.core.domain.usecase

import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.domain.repository.SavedDestinationRepository
import com.tripwise.core.domain.repository.TripRepository
import com.tripwise.core.model.TravelStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Profile header numbers: trips planned, destinations saved, distinct countries across trips. */
class ObserveTravelStatsUseCase @Inject constructor(
    private val tripRepository: TripRepository,
    private val savedRepository: SavedDestinationRepository,
    private val destinationRepository: DestinationRepository,
) {
    operator fun invoke(): Flow<TravelStats> = combine(
        tripRepository.observeTrips(),
        savedRepository.observeSavedIds(),
        destinationRepository.observeDestinations(),
    ) { trips, saved, destinations ->
        val countryById = destinations.associate { it.id to it.country }
        val countries = trips.mapNotNull { t -> t.destinationId?.let(countryById::get) }.toSet()
        TravelStats(trips = trips.size, saved = saved.size, countries = countries.size)
    }
}
