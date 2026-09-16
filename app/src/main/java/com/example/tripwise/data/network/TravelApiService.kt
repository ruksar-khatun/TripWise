package com.example.tripwise.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class ExchangeRateResponse(
    val result: String,
    val base_code: String,
    val conversion_rates: Map<String, Double>
)

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val current_weather: CurrentWeather?
)

data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val winddirection: Double,
    val weathercode: Int,
    val time: String
)

data class PlacesResponse(
    val results: List<PlaceResult>
)

data class PlaceResult(
    val name: String,
    val formatted_address: String?,
    val rating: Double?,
    val user_ratings_total: Int?,
    val price_level: Int?,
    val types: List<String>?
)

interface TravelApiService {
    @GET("v6/open/latest/{base}")
    suspend fun getExchangeRates(@Path("base") base: String): ExchangeRateResponse

    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") currentWeather: Boolean = true
    ): WeatherResponse

    @GET("maps/api/place/textsearch/json")
    suspend fun searchPlaces(
        @Query("query") query: String,
        @Query("type") type: String? = null,
        @Query("key") apiKey: String
    ): PlacesResponse

    companion object {
        private const val BASE_URL = "https://open.er-api.com/"

        fun create(): TravelApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TravelApiService::class.java)
        }
        
        fun createPlacesApi(): TravelApiService {
            return Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TravelApiService::class.java)
        }
    }
}
