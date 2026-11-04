package com.tripwise.core.data.mapper

import com.tripwise.core.database.entity.ItineraryItemEntity
import com.tripwise.core.database.entity.TripEntity
import com.tripwise.core.model.ActivityCategory
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.ImageRef
import com.tripwise.core.model.ItineraryItem
import com.tripwise.core.model.Money
import com.tripwise.core.model.Trip

internal fun money(minor: Long?, currency: String?): Money? =
    if (minor != null && currency != null) Money(minor, CurrencyCode(currency)) else null

internal fun TripEntity.toModel() = Trip(
    id = id,
    title = title,
    destinationId = destinationId,
    destinationName = destinationName,
    coverImage = coverImageUrl?.let { ImageRef(it) },
    startDate = startDate,
    endDate = endDate,
    travellers = travellers,
    budget = money(budgetMinor, budgetCurrency),
    notes = notes,
)

/** Unknown stored values (e.g. written by a newer app version) degrade to OTHER instead of crashing. */
internal inline fun <reified E : Enum<E>> enumOr(value: String, fallback: E): E =
    enumValues<E>().firstOrNull { it.name == value } ?: fallback

internal fun ItineraryItemEntity.toModel() = ItineraryItem(
    id = id,
    tripId = tripId,
    date = date,
    startTime = startTime,
    title = title,
    category = enumOr(category, ActivityCategory.OTHER),
    estimatedCost = money(costMinor, costCurrency),
    isDone = isDone,
    notes = notes,
)
