package com.tripwise.core.network.api

import kotlinx.serialization.Serializable
import retrofit2.http.GET

/** Retrofit contract for the TripWise backend. Feature endpoints are added per feature. */
interface TripWiseApi {
    @GET("api/health")
    suspend fun health(): HealthResponse
}

@Serializable
data class HealthResponse(
    val status: String,
    val service: String,
    val version: String,
)
