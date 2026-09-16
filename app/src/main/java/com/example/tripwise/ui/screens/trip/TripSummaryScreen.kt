package com.example.tripwise.ui.screens.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tripwise.data.AppState
import com.example.tripwise.ui.components.TripWiseCard
import com.example.tripwise.ui.components.TripWisePrimaryButton
import com.example.tripwise.ui.theme.AzureMist
import com.example.tripwise.ui.theme.DuskBlue
import com.example.tripwise.ui.theme.SeaBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripSummaryScreen(
    tripId: String,
    appState: AppState,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    appState.selectedTripId = tripId
    val trip = appState.selectedTrip

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzureMist)
    ) {
        TopAppBar(
            title = { Text("Trip Summary", fontWeight = FontWeight.Bold, color = DuskBlue) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DuskBlue)
                }
            },
            actions = {
                IconButton(onClick = onShareClick) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = SeaBlue)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AzureMist)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            TripWiseCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = trip.tripName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = trip.destinationName, fontSize = 15.sp, color = SeaBlue, fontWeight = FontWeight.SemiBold)

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = DuskBlue.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryItem("Dates", "${trip.startDateString}\nto ${trip.endDateString}")
                        SummaryItem("Travellers", "${trip.travellersCount} Persons")
                        SummaryItem("Activities", "${trip.activities.size} Planned")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Interests & Style", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
            Spacer(modifier = Modifier.height(12.dp))

            TripWiseCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Travel Style: ${trip.travelStyle}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DuskBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Interests: ${trip.interests.joinToString(", ")}", fontSize = 14.sp, color = DuskBlue.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            TripWisePrimaryButton(
                text = "Save & Share Trip Summary",
                onClick = onShareClick
            )
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 12.sp, color = DuskBlue.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
    }
}
