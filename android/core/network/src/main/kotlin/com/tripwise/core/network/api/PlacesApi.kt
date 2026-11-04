package com.tripwise.core.network.api

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable data class NearbyPlaceDto(
    val id: String,
    val name: String,
    val location: com.tripwise.core.network.dto.LatLngDto,
    val rating: Double? = null,
    val ratingCount: Int? = null,
    val category: String? = null,
    val address: String? = null,
    val googleMapsUri: String? = null,
)

@Serializable data class NearbyPlacesResponse(val places: List<NearbyPlaceDto>)

/** Authenticated: every call costs Places quota. */
interface PlacesApi {
    @GET("api/v1/places/nearby")
    suspend fun nearby(@Query("lat") lat: Double, @Query("lng") lng: Double, @Query("radius") radius: Int): NearbyPlacesResponse
}
