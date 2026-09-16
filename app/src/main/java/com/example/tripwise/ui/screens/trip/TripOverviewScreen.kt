package com.example.tripwise.ui.screens.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tripwise.data.AppState
import com.example.tripwise.ui.components.TripWiseCard
import com.example.tripwise.ui.theme.AzureMist
import com.example.tripwise.ui.theme.DuskBlue
import com.example.tripwise.ui.theme.SeaBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripOverviewScreen(
    tripId: String,
    appState: AppState,
    onBackClick: () -> Unit,
    onItineraryClick: () -> Unit,
    onBudgetClick: () -> Unit
) {
    appState.selectedTripId = tripId
    val trip = appState.selectedTrip

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzureMist)
    ) {
        TopAppBar(
            title = { Text(trip.tripName, fontWeight = FontWeight.Bold, color = DuskBlue) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DuskBlue)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AzureMist)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TripWiseCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = trip.destinationName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${trip.startDateString} to ${trip.endDateString} • ${trip.travellersCount} Travellers", fontSize = 13.sp, color = DuskBlue.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = DuskBlue.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Total Budget: ${appState.currencySymbol}${trip.totalBudgetLimit.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SeaBlue)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OverviewActionButton(
                title = "View Itinerary",
                subtitle = "${trip.activities.size} activities scheduled across days",
                icon = Icons.Default.EventNote,
                onClick = onItineraryClick
            )

            OverviewActionButton(
                title = "Trip Budget Breakdown",
                subtitle = "Track expenses across accommodation, food, transport",
                icon = Icons.Default.AccountBalanceWallet,
                onClick = onBudgetClick
            )

            OverviewActionButton(
                title = "Trip Summary & Export",
                subtitle = "Review complete trip plan and share with friends",
                icon = Icons.Default.Description,
                onClick = { /* Navigate or share */ }
            )
        }
    }
}

@Composable
fun OverviewActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    TripWiseCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(SeaBlue.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = SeaBlue, modifier = Modifier.size(26.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 12.sp, color = DuskBlue.copy(alpha = 0.65f))
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SeaBlue)
        }
    }
}
