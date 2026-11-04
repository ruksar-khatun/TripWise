package com.tripwise.core.network.datasource

import com.tripwise.core.common.result.Outcome
import com.tripwise.core.network.api.TripWiseApi
import com.tripwise.core.network.dto.HealthDto
import com.tripwise.core.network.error.ApiErrorMapper
import com.tripwise.core.network.error.call
import javax.inject.Inject
import javax.inject.Singleton

/** The only entry point :core:data uses for remote calls. Returns DTOs wrapped in [Outcome]. */
interface TripWiseNetworkDataSource {
    suspend fun health(): Outcome<HealthDto>
}

@Singleton
internal class RetrofitTripWiseNetworkDataSource @Inject constructor(
    private val api: TripWiseApi,
    private val errors: ApiErrorMapper,
) : TripWiseNetworkDataSource {
    override suspend fun health(): Outcome<HealthDto> = errors.call { api.health() }
}
