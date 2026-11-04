package com.tripwise.core.domain.repository

import com.tripwise.core.common.result.Outcome
import com.tripwise.core.model.BackendStatus

/**
 * Repository contracts live in :core:domain; implementations in :core:data.
 * Convention: observable data is exposed as `Flow<T>` (backed by Room once features exist),
 * one-shot operations as `suspend fun …: Outcome<T>`. Repositories never throw.
 */
interface SystemRepository {
    suspend fun checkBackend(): Outcome<BackendStatus>
}
