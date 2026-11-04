package com.tripwise.core.data.mapper

import com.tripwise.core.model.BackendStatus
import com.tripwise.core.network.dto.HealthDto

/** DTO → domain mappers live in :core:data so neither DTOs nor entities leak upward. */
internal fun HealthDto.toDomain() = BackendStatus(
    service = service,
    version = version,
    isHealthy = status.equals("ok", ignoreCase = true),
)
