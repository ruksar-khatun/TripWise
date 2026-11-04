package com.tripwise.core.network.api

import com.tripwise.core.network.dto.HealthDto
import retrofit2.http.GET

/**
 * Retrofit contract for the TripWise backend (`/api/v1`). Feature endpoints are added per phase.
 * Paths are relative (no leading slash) so they resolve against [com.tripwise.core.network.NetworkConfig.baseUrl].
 */
interface TripWiseApi {
    @GET("api/v1/health")
    suspend fun health(): HealthDto
}
