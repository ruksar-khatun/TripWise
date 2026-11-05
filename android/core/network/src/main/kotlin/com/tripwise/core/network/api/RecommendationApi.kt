package com.tripwise.core.network.api

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable data class RecommendationRequestDto(
    val destinationId: String,
    val kind: String,
    val budgetLevel: String,
    val travellers: Int,
    val interests: List<String>,
    val notes: String? = null,
)

@Serializable data class RecommendedPlaceDto(
    val name: String,
    val area: String = "",
    val type: String = "",
    val priceRangeInr: String = "",
    val description: String = "",
)

@Serializable data class RecommendationResponseDto(val kind: String, val destinationId: String, val places: List<RecommendedPlaceDto>)

interface RecommendationApi {
    @POST("api/v1/ai/recommendations") suspend fun recommend(@Body body: RecommendationRequestDto): RecommendationResponseDto
}
