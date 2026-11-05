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
    @Test fun `maps catalog dto, ignoring unknown enum values and converting rupees to paise`() {
        val dto = DestinationDto(
            id = "kyoto", name = "Kyoto", country = "Japan", region = "East Asia", summary = "s",
            highlights = listOf("h"), localFood = listOf("f"), interests = listOf("CULTURE", "SPACE"),
            location = LatLngDto(35.0, 135.7), rating = 4.8, ratingCount = 10, bestTimeToVisit = "Spring",
            currency = "JPY", languages = listOf("Japanese"), featured = true,
            images = listOf(CatalogImageDto("https://x/y.jpg", "A. Person", "https://unsplash.com", "Unsplash License")),
            attractions = listOf(AttractionDto("kyoto-1", "Fushimi Inari", "SIGHTSEEING", LatLngDto(1.0, 2.0), 150, 3.5, "d"),
                AttractionDto("kyoto-2", "Mystery", "TELEPORT", LatLngDto(1.0, 2.0), 30, 0.0, "d")),
            dailyCostInr = mapOf("FOOD" to 450.0, "UNKNOWN" to 1.0),
        )
        val d = dto.toModel()
        assertEquals(setOf(TravelInterest.CULTURE), d.interests)
        assertEquals("A. Person", d.coverImage?.attribution?.author)
        assertEquals(Money(350, CurrencyCode("INR")), d.attractions[0].estimatedCost)
        assertEquals(ActivityCategory.OTHER, d.attractions[1].category)
        assertEquals(mapOf(ExpenseCategory.FOOD to Money(45_000, CurrencyCode("INR"))), d.dailyCost)
        assertEquals("kyoto", d.attractions[0].destinationId)
    }
}
