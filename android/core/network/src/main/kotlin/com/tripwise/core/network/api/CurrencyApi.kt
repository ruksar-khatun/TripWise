package com.tripwise.core.network.api

import kotlinx.serialization.Serializable
import retrofit2.http.GET

@Serializable data class RatesDto(val base: String, val rates: Map<String, Double>, val source: String)

interface CurrencyApi {
    @GET("api/v1/currency/rates") suspend fun rates(): RatesDto
}
