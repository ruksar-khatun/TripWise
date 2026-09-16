package com.example.tripwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.tripwise.data.AppState
import com.example.tripwise.data.network.TravelRepository
import com.example.tripwise.ui.navigation.TripWiseNavGraph
import com.example.tripwise.ui.theme.TripWiseTheme
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Places SDK
        val apiKey = BuildConfig.MAPS_API_KEY
        if (apiKey.isNotBlank() && apiKey != "dummy_key") {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
        }
        
        setContent {
            val context = LocalContext.current
            val appState = remember { AppState(context) }
            val repository = remember { TravelRepository(context) }

            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (appState.themeMode) {
                "Light" -> false
                "Dark" -> true
                else -> systemDark
            }

            LaunchedEffect(appState.selectedCurrency) {
                withContext(Dispatchers.IO) {
                    val code = appState.getCurrencyCode()
                    val rate = repository.fetchExchangeRate("USD", code)
                    withContext(Dispatchers.Main) {
                        appState.currentExchangeRate = rate
                    }
                }
            }

            TripWiseTheme(darkTheme = darkTheme) {
                TripWiseNavGraph(appState = appState)
            }
        }
    }
}
