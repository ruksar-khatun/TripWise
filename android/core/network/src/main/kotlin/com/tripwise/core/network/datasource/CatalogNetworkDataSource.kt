package com.tripwise.core.network.datasource

import com.tripwise.core.common.result.Outcome
import com.tripwise.core.network.api.DestinationApi
import com.tripwise.core.network.dto.DestinationDto
import com.tripwise.core.network.error.ApiErrorMapper
import com.tripwise.core.network.error.call
import javax.inject.Inject
import javax.inject.Singleton

interface CatalogNetworkDataSource {
    /** The whole curated catalog (small enough to fetch at once and search on-device). */
    suspend fun fetchAll(): Outcome<List<DestinationDto>>
}

@Singleton
internal class RetrofitCatalogNetworkDataSource @Inject constructor(
    private val api: DestinationApi,
    private val errors: ApiErrorMapper,
) : CatalogNetworkDataSource {
    override suspend fun fetchAll() = errors.call {
        val all = mutableListOf<DestinationDto>()
        do {
            val page = api.list(limit = 100, offset = all.size)
            all += page.items
        } while (page.items.isNotEmpty() && all.size < page.total)
        all
    }
}
