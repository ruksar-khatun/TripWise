package com.tripwise.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class HealthDto(
    val status: String,
    val service: String,
    val version: String,
)
