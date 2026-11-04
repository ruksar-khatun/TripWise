package com.tripwise.core.network.api

import com.tripwise.core.network.dto.DestinationDto
import com.tripwise.core.network.dto.DestinationPageDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Public curated catalog. */
interface DestinationApi {
    @GET("api/v1/destinations")
    suspend fun list(
        @Query("query") query: String? = null,
        @Query("interest") interest: String? = null,
        @Query("featured") featured: Boolean? = null,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
    ): DestinationPageDto

    @GET("api/v1/destinations/{id}")
    suspend fun get(@Path("id") id: String): DestinationDto
}
