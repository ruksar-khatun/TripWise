package com.example.tripwise.ui.screens.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tripwise.data.AppState
import com.example.tripwise.data.Trip
import com.example.tripwise.data.TripStatus
import com.example.tripwise.ui.components.TripWiseCard
import com.example.tripwise.ui.theme.AzureMist
import com.example.tripwise.ui.theme.DuskBlue
import com.example.tripwise.ui.theme.SeaBlue
import com.example.tripwise.ui.theme.SeaSalt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTripsScreen(
    appState: AppState,
    onTripClick: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Upcoming, 1: Ongoing, 2: Completed

    val filteredTrips = appState.trips.filter { trip ->
        when (selectedTab) {
            0 -> trip.status == TripStatus.UPCOMING
            1 -> trip.status == TripStatus.ONGOING
            2 -> trip.status == TripStatus.COMPLETED
            else -> true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzureMist)
    ) {
        TopAppBar(
            title = { Text("My Trips", fontWeight = FontWeight.Bold, color = DuskBlue) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AzureMist)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = AzureMist,
            contentColor = SeaBlue
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Upcoming", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Ongoing", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Completed", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredTrips.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = DuskBlue.copy(alpha = 0.4f), modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "No trips found in this category", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredTrips) { trip ->
                    MyTripItemCard(
                        trip = trip,
                        currencySymbol = appState.currencySymbol,
                        onClick = {
                            appState.selectedTripId = trip.id
                            onTripClick(trip.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MyTripItemCard(
    trip: Trip,
    currencySymbol: String,
    onClick: () -> Unit
) {
    TripWiseCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.tripName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuskBlue
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (trip.status) {
                        TripStatus.UPCOMING -> SeaBlue.copy(alpha = 0.15f)
                        TripStatus.ONGOING -> Color(0xFFFF9800).copy(alpha = 0.15f)
                        TripStatus.COMPLETED -> SeaSalt.copy(alpha = 0.25f)
                    }
                ) {
                    Text(
                        text = trip.status.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (trip.status) {
                            TripStatus.UPCOMING -> SeaBlue
                            TripStatus.ONGOING -> Color(0xFFFF9800)
                            TripStatus.COMPLETED -> DuskBlue
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = SeaBlue, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = trip.destinationName, fontSize = 13.sp, color = DuskBlue.copy(alpha = 0.7f))
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = SeaBlue, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${trip.startDateString} - ${trip.endDateString}", fontSize = 13.sp, color = DuskBlue.copy(alpha = 0.7f))
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = DuskBlue.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budget: $currencySymbol${trip.totalBudgetLimit.toInt()}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SeaBlue
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "View Details", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = DuskBlue, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
