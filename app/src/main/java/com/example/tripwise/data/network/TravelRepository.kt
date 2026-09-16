package com.example.tripwise.data.network

import android.content.Context
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class WeatherInfo(
    val temperature: String,
    val condition: String,
    val wind: String,
    val lastUpdated: String
)

data class PlaceDetails(
    val rating: Double?,
    val reviewCount: Int?,
    val address: String,
    val priceLevel: Int?,
    val restaurants: List<String>,
    val hotels: List<String>,
    val attractions: List<String>
)

class TravelRepository(private val context: Context) {
    private val apiService = TravelApiService.create()
    private val weatherApiService = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TravelApiService::class.java)

    private val cachedRates = mutableMapOf<String, Double>()
    private val cachedWeather = mutableMapOf<String, WeatherInfo>()
    var lastUpdatedTime: String = "Offline (Default)"

    suspend fun fetchExchangeRate(base: String, target: String): Double {
        return try {
            val response = apiService.getExchangeRates(base)
            if (response.result == "success" || response.conversion_rates.isNotEmpty()) {
                val rate = response.conversion_rates[target] ?: 1.0
                cachedRates[target] = rate
                lastUpdatedTime = "Just now (Live API)"
                rate
            } else {
                cachedRates[target] ?: getFallbackRate(base, target)
            }
        } catch (e: Exception) {
            Log.e("TravelRepository", "Error fetching rates: ${e.message}")
            cachedRates[target] ?: getFallbackRate(base, target)
        }
    }

    suspend fun fetchWeather(cityName: String, lat: Double, lon: Double): WeatherInfo {
        return try {
            val response = weatherApiService.getWeather(lat, lon)
            val cw = response.current_weather
            val info = if (cw != null) {
                val conditionText = getWeatherConditionText(cw.weathercode)
                WeatherInfo(
                    temperature = "${cw.temperature}°C",
                    condition = conditionText,
                    wind = "${cw.windspeed} km/h",
                    lastUpdated = "Live API (${cw.time.takeLast(5)})"
                )
            } else {
                getDefaultWeather(cityName)
            }
            cachedWeather[cityName] = info
            info
        } catch (e: Exception) {
            Log.e("TravelRepository", "Error fetching weather: ${e.message}")
            cachedWeather[cityName] ?: getDefaultWeather(cityName)
        }
    }
    
    suspend fun fetchPlaceDetailsUsingSdk(query: String): PlaceDetails? {
        if (!Places.isInitialized()) return null
        
        val placesClient = Places.createClient(context)
        val placeFields = listOf(
            Place.Field.RATING,
            Place.Field.USER_RATINGS_TOTAL,
            Place.Field.ADDRESS,
            Place.Field.PRICE_LEVEL
        )
        
        return try {
            // First find the place ID via autocomplete/search
            val findRequest = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()
            
            val findResponse = placesClient.findAutocompletePredictions(findRequest).await()
            val placeId = findResponse.autocompletePredictions.firstOrNull()?.placeId ?: return null
            
            // Then fetch the actual details
            val fetchRequest = FetchPlaceRequest.builder(placeId, placeFields).build()
            val fetchResponse = placesClient.fetchPlace(fetchRequest).await()
            val place = fetchResponse.place
            
            PlaceDetails(
                rating = place.rating,
                reviewCount = place.userRatingsTotal,
                address = place.address ?: "",
                priceLevel = place.priceLevel,
                restaurants = listOf("Highly Rated Local Restaurant", "Popular Street Food Hub", "Traditional Dining"),
                hotels = listOf("Luxury Heritage Stay", "Boutique Resort", "Central City Hotel"),
                attractions = listOf("Historical Landmark", "Scenic Viewpoint", "Cultural Museum", "Famous Market")
            )
        } catch (e: Exception) {
            Log.e("TravelRepository", "Error with Places SDK: ${e.message}")
            null
        }
    }

    private fun getWeatherConditionText(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1, 2, 3 -> "Partly Cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rain Showers"
            71, 73, 75 -> "Snowfall"
            95, 96, 99 -> "Thunderstorm"
            else -> "Pleasant"
        }
    }

    private fun getDefaultWeather(cityName: String): WeatherInfo {
        return when {
            cityName.contains("Munnar", true) -> WeatherInfo("22°C", "Misty & Pleasant", "8 km/h", "Cached (Offline)")
            cityName.contains("Goa", true) -> WeatherInfo("31°C", "Sunny & Humid", "14 km/h", "Cached (Offline)")
            cityName.contains("Jaipur", true) -> WeatherInfo("34°C", "Warm & Clear", "12 km/h", "Cached (Offline)")
            cityName.contains("Manali", true) -> WeatherInfo("15°C", "Cool Mountain Breeze", "10 km/h", "Cached (Offline)")
            cityName.contains("Coorg", true) -> WeatherInfo("24°C", "Breezy Showers", "9 km/h", "Cached (Offline)")
            else -> WeatherInfo("26°C", "Clear & Sunny", "11 km/h", "Cached (Offline)")
        }
    }

    private fun getFallbackRate(base: String, target: String): Double {
        return when ("$base->$target") {
            "USD->INR" -> 83.0
            "INR->USD" -> 0.012
            "EUR->INR" -> 90.0
            "INR->EUR" -> 0.011
            "GBP->INR" -> 105.0
            "INR->GBP" -> 0.0095
            "JPY->INR" -> 0.55
            "INR->JPY" -> 1.82
            "SGD->INR" -> 62.0
            "INR->SGD" -> 0.016
            else -> 1.0
        }
    }
}
