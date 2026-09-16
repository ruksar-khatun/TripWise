package com.example.tripwise.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tripwise.data.AppState
import com.example.tripwise.data.network.TravelRepository
import com.example.tripwise.data.network.WeatherInfo
import com.example.tripwise.data.network.PlaceDetails
import com.example.tripwise.ui.components.TripWiseCard
import com.example.tripwise.ui.components.TripWiseOutlinedButton
import com.example.tripwise.ui.components.TripWisePrimaryButton
import com.example.tripwise.ui.theme.AzureMist
import com.example.tripwise.ui.theme.DuskBlue
import com.example.tripwise.ui.theme.SeaBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DestinationDetailsScreen(
    destinationId: String,
    appState: AppState,
    onBackClick: () -> Unit,
    onAddToTripClick: () -> Unit
) {
    val context = LocalContext.current
    val destination = appState.destinations.firstOrNull { it.id == destinationId }
        ?: appState.selectedDestination
    val currency = appState.currencySymbol

    var weatherInfo by remember { mutableStateOf<WeatherInfo?>(null) }
    var placeDetails by remember { mutableStateOf<PlaceDetails?>(null) }
    val repository = remember { TravelRepository(context) }

    // Coordinates mapping for weather API
    val (lat, lon) = when (destination.id) {
        "munnar_india" -> 10.0889 to 77.0595
        "goa_india" -> 15.2993 to 74.1240
        "jaipur_india" -> 26.9124 to 75.7873
        "manali_india" -> 32.2432 to 77.1892
        "coorg_india" -> 12.3375 to 75.8069
        "ooty_india" -> 11.4102 to 76.6950
        "tokyo_japan" -> 35.6895 to 139.6917
        "seoul_sk" -> 37.5665 to 126.9780
        "bangkok_thailand" -> 13.7563 to 100.5018
        "singapore_sg" -> 1.3521 to 103.8198
        "dubai_uae" -> 25.2048 to 55.2708
        else -> 20.0 to 78.0
    }

    LaunchedEffect(destination.id) {
        withContext(Dispatchers.IO) {
            val wInfo = repository.fetchWeather(destination.name, lat, lon)
            val pInfo = repository.fetchPlaceDetailsUsingSdk("${destination.name} ${destination.country}")
            withContext(Dispatchers.Main) {
                weatherInfo = wInfo
                placeDetails = pInfo
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AzureMist)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // Hero Image Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                Image(
                    painter = painterResource(id = destination.imageRes),
                    contentDescription = destination.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Navigation overlay bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DuskBlue)
                    }

                    IconButton(
                        onClick = { appState.toggleSaveDestination(destination.id) },
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (destination.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (destination.isSaved) SeaBlue else DuskBlue
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = destination.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuskBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = SeaBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${destination.location}, ${destination.country}",
                                fontSize = 14.sp,
                                color = DuskBlue.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SeaBlue.copy(alpha = 0.12f),
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = placeDetails?.rating?.toString() ?: "${destination.rating}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuskBlue
                            )
                        }
                    }
                }

                if (placeDetails != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Based on ${placeDetails!!.reviewCount ?: destination.reviews} Google Reviews", fontSize = 12.sp, color = DuskBlue.copy(alpha=0.6f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real-Time Weather Card
                if (weatherInfo != null) {
                    TripWiseCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Current Weather", fontSize = 12.sp, color = DuskBlue.copy(alpha = 0.6f))
                                    Text(text = "${weatherInfo!!.temperature} • ${weatherInfo!!.condition}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                                    Text(text = "Wind: ${weatherInfo!!.wind} • ${weatherInfo!!.lastUpdated}", fontSize = 11.sp, color = DuskBlue.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Description & Why Visit
                Text(text = "About", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = destination.about.ifEmpty { destination.description }, fontSize = 14.sp, color = DuskBlue.copy(alpha = 0.8f), lineHeight = 22.sp)

                if (destination.whyVisit.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Why Visit", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = destination.whyVisit, fontSize = 14.sp, color = DuskBlue.copy(alpha = 0.8f), lineHeight = 22.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Things to Do
                val activities = if (destination.thingsToDo.isNotEmpty()) destination.thingsToDo else placeDetails?.attractions ?: emptyList()
                if (activities.isNotEmpty()) {
                    Text(text = "Main Attractions & Things to Do", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                    Spacer(modifier = Modifier.height(12.dp))
                    activities.forEach { act ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SeaBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = act, fontSize = 14.sp, color = DuskBlue)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Local Food & Restaurants
                val restaurants = placeDetails?.restaurants ?: emptyList()
                if (destination.localFood.isNotEmpty() || restaurants.isNotEmpty()) {
                    Text(text = "Local Food & Dining", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (destination.localFood.isNotEmpty()) {
                        Text(text = "Must Try: ${destination.localFood}", fontSize = 14.sp, color = DuskBlue.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    restaurants.forEach { rest ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = SeaBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = rest, fontSize = 14.sp, color = DuskBlue)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Accommodation
                val hotels = placeDetails?.hotels ?: emptyList()
                if (destination.accommodation.isNotEmpty() || hotels.isNotEmpty()) {
                    Text(text = "Accommodation", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (destination.accommodation.isNotEmpty()) {
                        Text(text = "Highly Recommended: ${destination.accommodation}", fontSize = 14.sp, color = DuskBlue.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    hotels.forEach { hotel ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                            Icon(Icons.Default.Hotel, contentDescription = null, tint = SeaBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = hotel, fontSize = 14.sp, color = DuskBlue)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Transportation
                if (destination.transport.isNotEmpty()) {
                    Text(text = "Local Transportation", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = destination.transport, fontSize = 14.sp, color = DuskBlue.copy(alpha = 0.8f), lineHeight = 20.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Estimated Cost Card
                TripWiseCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Estimated Trip Cost", fontSize = 13.sp, color = DuskBlue.copy(alpha = 0.6f))
                            Text(
                                text = "$currency${(destination.estimatedCost * appState.currentExchangeRate).toInt()} / person",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = SeaBlue
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DuskBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = destination.category,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DuskBlue,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Fixed Bottom Actions
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shadowElevation = 16.dp,
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TripWiseOutlinedButton(
                    text = if (destination.isSaved) "Saved" else "Save Place",
                    onClick = { appState.toggleSaveDestination(destination.id) },
                    modifier = Modifier.weight(1f)
                )

                TripWisePrimaryButton(
                    text = "Add to Trip",
                    onClick = {
                        appState.selectedDestinationId = destination.id
                        onAddToTripClick()
                    },
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
    }
}
