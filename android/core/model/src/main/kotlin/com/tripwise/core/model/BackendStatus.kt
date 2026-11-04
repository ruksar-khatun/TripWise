package com.tripwise.core.model

/** Result of the backend health check. Feature models (Trip, Destination, Expense, …) arrive with their phases. */
data class BackendStatus(
    val service: String,
    val version: String,
    val isHealthy: Boolean,
)
