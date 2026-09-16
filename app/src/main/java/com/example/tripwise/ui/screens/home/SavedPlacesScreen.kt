package com.example.tripwise.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tripwise.data.AppState
import com.example.tripwise.data.Destination
import com.example.tripwise.ui.components.TripWiseCard
import com.example.tripwise.ui.theme.AzureMist
import com.example.tripwise.ui.theme.DuskBlue
import com.example.tripwise.ui.theme.SeaBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPlacesScreen(
    appState: AppState,
    onDestinationClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val savedDestinations = appState.destinations.filter { it.isSaved }
    val currency = appState.currencySymbol

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzureMist)
    ) {
        TopAppBar(
            title = { Text("Saved Places (${savedDestinations.size})", fontWeight = FontWeight.Bold, color = DuskBlue) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DuskBlue)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AzureMist)
        )

        if (savedDestinations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = DuskBlue.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No saved places yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuskBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Explore destinations and tap the bookmark icon to save places here.",
                        fontSize = 14.sp,
                        color = DuskBlue.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(savedDestinations) { destination ->
                    SavedDestinationCard(
                        destination = destination,
                        currencySymbol = currency,
                        currentExchangeRate = appState.currentExchangeRate,
                        onUnsave = { appState.toggleSaveDestination(destination.id) },
                        onClick = { onDestinationClick(destination.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SavedDestinationCard(
    destination: Destination,
    currencySymbol: String,
    currentExchangeRate: Double,
    onUnsave: () -> Unit,
    onClick: () -> Unit
) {
    TripWiseCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = destination.imageRes),
                contentDescription = destination.name,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = destination.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuskBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = SeaBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${destination.location}, ${destination.country}",
                        fontSize = 12.sp,
                        color = DuskBlue.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$currencySymbol${(destination.estimatedCost * currentExchangeRate).toInt()} / person",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SeaBlue
                )
            }

            IconButton(onClick = onUnsave) {
                Icon(Icons.Default.Bookmark, contentDescription = "Unsave", tint = SeaBlue)
            }
        }
    }
}
