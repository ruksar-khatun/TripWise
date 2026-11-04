package com.tripwise.core.data.mapper

import com.tripwise.core.model.ActivityCategory
import com.tripwise.core.model.Attraction
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.Destination
import com.tripwise.core.model.ExpenseCategory
import com.tripwise.core.model.GeoPoint
import com.tripwise.core.model.ImageAttribution
import com.tripwise.core.model.ImageRef
import com.tripwise.core.model.Money
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.network.dto.DestinationDto
import kotlin.math.roundToLong

private val USD = CurrencyCode("USD")

internal fun usd(amount: Double) = Money((amount * 100).roundToLong(), USD)

internal fun DestinationDto.toModel() = Destination(
    id = id,
    name = name,
    country = country,
    region = region,
    summary = summary,
    highlights = highlights,
    localFood = localFood,
    images = images.map { ImageRef(it.url, ImageAttribution(it.author, it.sourceUrl, it.license)) },
    location = GeoPoint(location.lat, location.lng),
    interests = interests.mapNotNull { name -> TravelInterest.entries.firstOrNull { it.name == name } }.toSet(),
    rating = rating,
    ratingCount = ratingCount,
    bestTimeToVisit = bestTimeToVisit,
    currency = CurrencyCode(currency),
    languages = languages,
    featured = featured,
    attractions = attractions.map { a ->
        Attraction(
            id = a.id,
            destinationId = id,
            name = a.name,
            category = enumOr(a.category, ActivityCategory.OTHER),
            location = GeoPoint(a.location.lat, a.location.lng),
            durationMinutes = a.durationMinutes,
            estimatedCost = usd(a.estimatedCostUsd),
            description = a.description,
        )
    },
    dailyCostUsd = dailyCostUsd.mapNotNull { (key, value) ->
        ExpenseCategory.entries.firstOrNull { it.name == key }?.let { it to usd(value) }
    }.toMap(),
)
