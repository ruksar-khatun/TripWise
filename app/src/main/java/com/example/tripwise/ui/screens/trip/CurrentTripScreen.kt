package com.example.tripwise.ui.screens.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun CurrentTripScreen(
    appState: AppState,
    onActivityClick: (String) -> Unit = {}
) {
    val activeTrip = appState.trips.firstOrNull { it.status.name == "ONGOING" } ?: appState.trips.first()
    val todayActivities = activeTrip.activities

    val completedCount = todayActivities.count { it.isCompleted }
    val progress = if (todayActivities.isNotEmpty()) completedCount.toFloat() / todayActivities.size else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzureMist)
    ) {
        TopAppBar(
            title = { Text("Today's Schedule", fontWeight = FontWeight.Bold, color = DuskBlue) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AzureMist)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Live Status Card
            TripWiseCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = activeTrip.tripName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = activeTrip.destinationName, fontSize = 13.sp, color = SeaBlue, fontWeight = FontWeight.SemiBold)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SeaBlue.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "LIVE TODAY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SeaBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SeaBlue,
                        trackColor = SeaBlue.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$completedCount of ${todayActivities.size} activities completed",
                        fontSize = 12.sp,
                        color = DuskBlue.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Today's Itinerary Timeline",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DuskBlue
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(todayActivities) { activity ->
                    ActivityTimelineCard(
                        activity = activity,
                        currencySymbol = appState.currencySymbol,
                        onToggleCompleted = { appState.toggleActivityCompleted(activity.id) },
                        onDelete = { appState.deleteActivityFromSelectedTrip(activity.id) },
                        onClick = {
                            appState.selectedActivityId = activity.id
                            onActivityClick(activity.id)
                        }
                    )
                }
            }
        }
    }
}
