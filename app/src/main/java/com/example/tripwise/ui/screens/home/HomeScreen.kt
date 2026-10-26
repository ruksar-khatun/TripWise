package com.example.tripwise.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tripwise.data.AppState
import com.example.tripwise.data.Destination
import com.example.tripwise.ui.components.SectionHeader
import com.example.tripwise.ui.components.TripWiseCard
import com.example.tripwise.ui.theme.AzureMist
import com.example.tripwise.ui.theme.DuskBlue
import com.example.tripwise.ui.theme.SeaBlue
import com.example.tripwise.ui.theme.SeaSalt

@Composable
fun HomeScreen(
    appState: AppState,
    onDestinationClick: (String) -> Unit,
    onExploreClick: () -> Unit,
    onProfileClick: () -> Unit,
    onCreateTripClick: () -> Unit,
    onSavedPlacesClick: () -> Unit,
    onCurrentTripClick: () -> Unit,
    onAiAssistantClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val user = appState.userProfile
    val upcomingTrip = appState.trips.firstOrNull { it.id == "t101" } ?: appState.trips.firstOrNull()
    val currency = appState.currencySymbol

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzureMist)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hello, ${user.name.split(" ").first()}! 👋",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuskBlue
                )
                Text(
                    text = "Where would you like to travel next?",
                    fontSize = 14.sp,
                    color = DuskBlue.copy(alpha = 0.7f)
                )
            }

            IconButton(
                onClick = onProfileClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            ) {
                if (user.avatarUri != null) {
                    AsyncImage(
                        model = user.avatarUri,
                        contentDescription = "Profile Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = user.avatarRes),
                        contentDescription = "Profile Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search places, cities, attractions...", color = DuskBlue.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SeaBlue) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = DuskBlue)
                    }
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clickable { onExploreClick() },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = SeaBlue,
                unfocusedBorderColor = DuskBlue.copy(alpha = 0.2f)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Actions Row (Scrollable to prevent overflow on narrow devices)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            QuickActionButton("Ask AI", Icons.Default.AutoAwesome, SeaBlue, onAiAssistantClick)
            QuickActionButton("New Trip", Icons.Default.Add, SeaBlue, onCreateTripClick)
            QuickActionButton("Explore", Icons.Default.Explore, DuskBlue, onExploreClick)
            QuickActionButton("Saved", Icons.Default.Bookmark, SeaSalt, onSavedPlacesClick)
            QuickActionButton("Today", Icons.Default.Today, SeaBlue, onCurrentTripClick)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Ask TripWise AI Banner
        TripWiseCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            onClick = onAiAssistantClick
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SeaBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = SeaBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ask TripWise AI Assistant ✨",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuskBlue
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Get custom itineraries, local food tips & budget advice",
                        fontSize = 12.sp,
                        color = DuskBlue.copy(alpha = 0.7f)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = SeaBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Upcoming Trip Card
        if (upcomingTrip != null) {
            SectionHeader(
                title = "Upcoming Trip",
                actionText = "Manage",
                onActionClick = { /* Go to trip overview */ },
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TripWiseCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                onClick = onCurrentTripClick
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SeaBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = SeaBlue, modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = upcomingTrip.tripName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuskBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${upcomingTrip.destinationName} • ${upcomingTrip.startDateString}",
                            fontSize = 13.sp,
                            color = DuskBlue.copy(alpha = 0.7f)
                        )
                    }

                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SeaBlue)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Popular Destinations
        SectionHeader(
            title = "Popular Destinations",
            actionText = "See All",
            onActionClick = onExploreClick,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(appState.destinations) { destination ->
                DestinationCard(
                    destination = destination,
                    currencySymbol = currency,
                    currentExchangeRate = appState.currentExchangeRate,
                    onSaveToggle = { appState.toggleSaveDestination(destination.id) },
                    onClick = { onDestinationClick(destination.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Recommended for You
        SectionHeader(
            title = "Recommended for You",
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        appState.destinations.takeLast(3).forEach { destination ->
            RecommendationItem(
                destination = destination,
                currencySymbol = currency,
                currentExchangeRate = appState.currentExchangeRate,
                onSaveToggle = { appState.toggleSaveDestination(destination.id) },
                onClick = { onDestinationClick(destination.id) },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DuskBlue)
    }
}

@Composable
fun DestinationCard(
    destination: Destination,
    currencySymbol: String,
    currentExchangeRate: Double,
    onSaveToggle: () -> Unit,
    onClick: () -> Unit
) {
    TripWiseCard(
        modifier = Modifier.width(210.dp),
        onClick = onClick
    ) {
        Box {
            Image(
                painter = painterResource(id = destination.imageRes),
                contentDescription = destination.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentScale = ContentScale.Crop
            )

            IconButton(
                onClick = onSaveToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
            ) {
                Icon(
                    imageVector = if (destination.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (destination.isSaved) SeaBlue else DuskBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = destination.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DuskBlue,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = SeaBlue, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${destination.location}, ${destination.country}",
                    fontSize = 12.sp,
                    color = DuskBlue.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$currencySymbol${(destination.estimatedCost * currentExchangeRate).toInt()} est.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SeaBlue
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "${destination.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                }
            }
        }
    }
}

@Composable
fun RecommendationItem(
    destination: Destination,
    currencySymbol: String,
    currentExchangeRate: Double,
    onSaveToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TripWiseCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = destination.imageRes),
                contentDescription = destination.name,
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = destination.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuskBlue
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${destination.category} • ${destination.country}",
                    fontSize = 12.sp,
                    color = DuskBlue.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "From $currencySymbol${(destination.estimatedCost * currentExchangeRate).toInt()}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SeaBlue
                )
            }

            IconButton(onClick = onSaveToggle) {
                Icon(
                    imageVector = if (destination.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (destination.isSaved) SeaBlue else DuskBlue
                )
            }
        }
    }
}
