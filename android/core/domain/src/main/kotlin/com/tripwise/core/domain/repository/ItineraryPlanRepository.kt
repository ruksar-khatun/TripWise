package com.tripwise.core.domain.repository

import com.tripwise.core.common.result.Outcome
import com.tripwise.core.model.ItineraryPlan
import com.tripwise.core.model.ItineraryRequest

interface ItineraryPlanRepository {
    suspend fun generate(request: ItineraryRequest): Outcome<ItineraryPlan>
}
