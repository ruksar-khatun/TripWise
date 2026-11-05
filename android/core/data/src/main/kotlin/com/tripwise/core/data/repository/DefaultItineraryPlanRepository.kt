package com.tripwise.core.data.repository

import com.tripwise.core.common.result.Outcome
import com.tripwise.core.common.result.map
import com.tripwise.core.data.mapper.enumOr
import com.tripwise.core.data.mapper.inr
import com.tripwise.core.domain.repository.ItineraryPlanRepository
import com.tripwise.core.model.ActivityCategory
import com.tripwise.core.model.ItineraryPlan
import com.tripwise.core.model.ItineraryRequest
import com.tripwise.core.model.PlanDay
import com.tripwise.core.model.PlanItem
import com.tripwise.core.model.PlanSource
import com.tripwise.core.network.api.ItineraryApi
import com.tripwise.core.network.api.ItineraryRequestDto
import com.tripwise.core.network.error.ApiErrorMapper
import com.tripwise.core.network.error.call
import java.time.LocalTime
import javax.inject.Inject

internal class DefaultItineraryPlanRepository @Inject constructor(
    private val api: ItineraryApi,
    private val errors: ApiErrorMapper,
) : ItineraryPlanRepository {
    override suspend fun generate(request: ItineraryRequest): Outcome<ItineraryPlan> = errors.call {
        api.generate(
            ItineraryRequestDto(
                destinationId = request.destinationId,
                days = request.days,
                travellers = request.travellers,
                pace = request.pace.name,
                budgetLevel = request.budgetLevel.name,
                interests = request.interests.map { it.name },
                notes = request.notes?.trim()?.ifEmpty { null },
            ),
        )
    }.map { dto ->
        ItineraryPlan(
            destinationId = dto.destinationId,
            title = dto.title,
            summary = dto.summary,
            source = if (dto.source == "ai") PlanSource.AI else PlanSource.TEMPLATE,
            days = dto.days.map { day ->
                PlanDay(day.day, day.theme, day.items.map { item ->
                    PlanItem(
                        startTime = runCatching { LocalTime.parse(item.startTime) }.getOrDefault(LocalTime.NOON),
                        title = item.title,
                        category = enumOr(item.category, ActivityCategory.OTHER),
                        durationMinutes = item.durationMinutes,
                        estimatedCost = inr(item.estimatedCostInr),
                        notes = item.notes,
                        attractionId = item.attractionId,
                    )
                })
            },
        )
    }
}
