package com.tripwise.core.data.repository

import com.tripwise.core.common.result.Outcome
import com.tripwise.core.common.result.map
import com.tripwise.core.domain.repository.PlacesRepository
import com.tripwise.core.model.GeoPoint
import com.tripwise.core.model.NearbyPlace
import com.tripwise.core.network.api.PlacesApi
import com.tripwise.core.network.error.ApiErrorMapper
import com.tripwise.core.network.error.call
import javax.inject.Inject

internal class DefaultPlacesRepository @Inject constructor(
    private val api: PlacesApi,
    private val errors: ApiErrorMapper,
) : PlacesRepository {
    override suspend fun nearby(center: GeoPoint, radiusMeters: Int): Outcome<List<NearbyPlace>> =
        errors.call { api.nearby(center.latitude, center.longitude, radiusMeters) }.map { res ->
            res.places.map {
                NearbyPlace(it.id, it.name, GeoPoint(it.location.lat, it.location.lng), it.rating, it.ratingCount,
                    it.category, it.address, it.googleMapsUri)
            }
        }
}
