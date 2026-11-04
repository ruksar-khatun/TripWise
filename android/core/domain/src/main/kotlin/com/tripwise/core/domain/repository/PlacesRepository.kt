package com.tripwise.core.domain.repository

import com.tripwise.core.common.result.Outcome
import com.tripwise.core.model.GeoPoint
import com.tripwise.core.model.NearbyPlace

interface PlacesRepository {
    /** Popular attractions around [center] (Google Places via the backend). */
    suspend fun nearby(center: GeoPoint, radiusMeters: Int = 5_000): Outcome<List<NearbyPlace>>
}
