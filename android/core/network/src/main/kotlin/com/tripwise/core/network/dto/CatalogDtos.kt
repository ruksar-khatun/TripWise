package com.tripwise.core.network.dto

import kotlinx.serialization.Serializable

@Serializable data class LatLngDto(val lat: Double, val lng: Double)

@Serializable
data class CatalogImageDto(val url: String, val author: String, val sourceUrl: String, val license: String)

@Serializable
data class AttractionDto(
    val id: String,
    val name: String,
    val category: String,
    val location: LatLngDto,
    val durationMinutes: Int,
    val estimatedCostInr: Double,
    val description: String,
)

@Serializable
data class DestinationDto(
    val id: String,
    val name: String,
    val country: String,
    val region: String,
    val summary: String,
    val highlights: List<String>,
    val localFood: List<String>,
    val interests: List<String>,
    val location: LatLngDto,
    val rating: Double? = null,
    val ratingCount: Int? = null,
    val bestTimeToVisit: String,
    val currency: String,
    val languages: List<String>,
    val featured: Boolean = false,
    val popularity: Int = 0,
    val images: List<CatalogImageDto> = emptyList(),
    val attractions: List<AttractionDto> = emptyList(),
    val dailyCostInr: Map<String, Double> = emptyMap(),
)

@Serializable
data class DestinationPageDto(val items: List<DestinationDto>, val total: Int, val limit: Int, val offset: Int)
