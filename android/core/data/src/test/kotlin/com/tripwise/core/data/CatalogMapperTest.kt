package com.tripwise.core.data

import com.tripwise.core.data.mapper.toModel
import com.tripwise.core.model.ActivityCategory
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.ExpenseCategory
import com.tripwise.core.model.Money
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.network.dto.AttractionDto
import com.tripwise.core.network.dto.CatalogImageDto
import com.tripwise.core.network.dto.DestinationDto
import com.tripwise.core.network.dto.LatLngDto
import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogMapperTest {
    @Test fun `maps catalog dto, ignoring unknown enum values and converting USD to cents`() {
        val dto = DestinationDto(
            id = "kyoto", name = "Kyoto", country = "Japan", region = "East Asia", summary = "s",
            highlights = listOf("h"), localFood = listOf("f"), interests = listOf("CULTURE", "SPACE"),
            location = LatLngDto(35.0, 135.7), rating = 4.8, ratingCount = 10, bestTimeToVisit = "Spring",
            currency = "JPY", languages = listOf("Japanese"), featured = true,
            images = listOf(CatalogImageDto("https://x/y.jpg", "A. Person", "https://unsplash.com", "Unsplash License")),
            attractions = listOf(AttractionDto("kyoto-1", "Fushimi Inari", "SIGHTSEEING", LatLngDto(1.0, 2.0), 150, 3.5, "d"),
                AttractionDto("kyoto-2", "Mystery", "TELEPORT", LatLngDto(1.0, 2.0), 30, 0.0, "d")),
            dailyCostUsd = mapOf("FOOD" to 45.0, "UNKNOWN" to 1.0),
        )
        val d = dto.toModel()
        assertEquals(setOf(TravelInterest.CULTURE), d.interests)
        assertEquals("A. Person", d.coverImage?.attribution?.author)
        assertEquals(Money(350, CurrencyCode("USD")), d.attractions[0].estimatedCost)
        assertEquals(ActivityCategory.OTHER, d.attractions[1].category)
        assertEquals(mapOf(ExpenseCategory.FOOD to Money(4_500, CurrencyCode("USD"))), d.dailyCostUsd)
        assertEquals("kyoto", d.attractions[0].destinationId)
    }
}
