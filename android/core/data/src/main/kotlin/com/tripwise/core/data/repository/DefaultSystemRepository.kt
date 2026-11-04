package com.tripwise.core.data.repository

import com.tripwise.core.common.result.Outcome
import com.tripwise.core.common.result.map
import com.tripwise.core.data.mapper.toDomain
import com.tripwise.core.domain.repository.SystemRepository
import com.tripwise.core.model.BackendStatus
import com.tripwise.core.network.datasource.TripWiseNetworkDataSource
import javax.inject.Inject

internal class DefaultSystemRepository @Inject constructor(
    private val network: TripWiseNetworkDataSource,
) : SystemRepository {
    override suspend fun checkBackend(): Outcome<BackendStatus> = network.health().map { it.toDomain() }
}
