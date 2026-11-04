package com.tripwise.core.network.api

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable data class ItineraryRequestDto(
    val destinationId: String,
    val days: Int,
    val travellers: Int,
    val pace: String,
    val budgetLevel: String,
    val interests: List<String>,
    val notes: String? = null,
)

@Serializable data class PlanItemDto(
    val startTime: String,
    val title: String,
    val category: String,
    val durationMinutes: Int,
    val estimatedCostUsd: Double,
    val notes: String? = null,
    val attractionId: String? = null,
)

@Serializable data class PlanDayDto(val day: Int, val theme: String, val items: List<PlanItemDto>)

@Serializable data class ItineraryPlanDto(
    val destinationId: String,
    val title: String,
    val summary: String,
    val source: String,
    val days: List<PlanDayDto>,
)

interface ItineraryApi {
    @POST("api/v1/ai/itineraries") suspend fun generate(@Body body: ItineraryRequestDto): ItineraryPlanDto
}
