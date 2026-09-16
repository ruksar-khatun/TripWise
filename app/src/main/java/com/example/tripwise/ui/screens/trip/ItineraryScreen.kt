package com.example.tripwise.ui.screens.trip

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.tripwise.data.AppState
import com.example.tripwise.data.TravelActivity
import com.example.tripwise.ui.components.TripWiseCard
import com.example.tripwise.ui.components.TripWisePrimaryButton
import com.example.tripwise.ui.components.TripWiseTextField
import com.example.tripwise.ui.theme.AzureMist
import com.example.tripwise.ui.theme.DuskBlue
import com.example.tripwise.ui.theme.SeaBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryScreen(
    tripId: String,
    appState: AppState,
    onBackClick: () -> Unit,
    onActivityClick: (String) -> Unit,
    onAddActivityClick: () -> Unit = {}
) {
    appState.selectedTripId = tripId
    val trip = appState.selectedTrip

    var selectedDayIndex by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Dialog inputs
    var titleInput by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("10:00") }
    var durationInput by remember { mutableStateOf("2.0 hrs") }
    var costInput by remember { mutableStateOf("30") }

    fun submitNewActivity() {
        if (titleInput.isBlank()) return
        val newActivity = TravelActivity(
            id = "act_${System.currentTimeMillis()}",
            dayIndex = selectedDayIndex,
            title = titleInput,
            location = if (locationInput.isBlank()) trip.destinationName else locationInput,
            description = descInput,
            time = timeInput,
            duration = durationInput,
            cost = costInput.toDoubleOrNull() ?: 0.0,
            isCompleted = false
        )
        appState.addActivityToSelectedTrip(newActivity)
        showAddDialog = false
        titleInput = ""
        locationInput = ""
        descInput = ""
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            confirmButton = {
                Button(
                    onClick = { submitNewActivity() },
                    colors = ButtonDefaults.buttonColors(containerColor = SeaBlue)
                ) {
                    Text("Add Activity")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = DuskBlue)
                }
            },
            title = { Text("Add Activity - Day ${selectedDayIndex + 1}", fontWeight = FontWeight.Bold, color = DuskBlue) },
            text = {
                Column {
                    TripWiseTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = "Activity Title"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TripWiseTextField(
                        value = locationInput,
                        onValueChange = { locationInput = it },
                        label = "Location"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TripWiseTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = "Description"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        TripWiseTextField(
                            value = timeInput,
                            onValueChange = { timeInput = it },
                            label = "Time",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TripWiseTextField(
                            value = costInput,
                            onValueChange = { costInput = it },
                            label = "Cost (${appState.currencySymbol})",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        )
    }

    val currentDayActivities = trip.activities.filter { it.dayIndex == selectedDayIndex }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzureMist)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(trip.tripName, fontWeight = FontWeight.Bold, color = DuskBlue, fontSize = 18.sp)
                    Text(trip.destinationName, fontSize = 12.sp, color = DuskBlue.copy(alpha = 0.6f))
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DuskBlue)
                }
            },
            actions = {
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Activity", tint = SeaBlue)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AzureMist)
        )

        // Day Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedDayIndex,
            containerColor = AzureMist,
            contentColor = SeaBlue,
            edgePadding = 24.dp
        ) {
            repeat(4) { index ->
                Tab(
                    selected = selectedDayIndex == index,
                    onClick = { selectedDayIndex = index },
                    text = {
                        Text(
                            text = "Day ${index + 1}",
                            fontWeight = if (selectedDayIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedDayIndex == index) SeaBlue else DuskBlue
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (currentDayActivities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EventNote, contentDescription = null, tint = DuskBlue.copy(alpha = 0.4f), modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No activities planned for Day ${selectedDayIndex + 1}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    TripWisePrimaryButton(
                        text = "+ Add First Activity",
                        onClick = { showAddDialog = true },
                        modifier = Modifier.width(220.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(currentDayActivities) { activity ->
                    ActivityTimelineCard(
                        activity = activity,
                        onToggleCompleted = { appState.toggleActivityCompleted(activity.id) },
                        onDelete = { appState.deleteActivityFromSelectedTrip(activity.id) },
                        currencySymbol = appState.currencySymbol,
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

@Composable
fun ActivityTimelineCard(
    activity: TravelActivity,
    currencySymbol: String,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    TripWiseCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = activity.isCompleted,
                onCheckedChange = { onToggleCompleted() },
                colors = CheckboxDefaults.colors(checkedColor = SeaBlue)
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (activity.imageRes != null) {
                Image(
                    painter = painterResource(id = activity.imageRes!!),
                    contentDescription = activity.title,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuskBlue
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = SeaBlue, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${activity.time} • ${activity.duration}", fontSize = 12.sp, color = DuskBlue.copy(alpha = 0.65f))
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = SeaBlue, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = activity.location, fontSize = 12.sp, color = DuskBlue.copy(alpha = 0.65f), maxLines = 1)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (activity.cost > 0) "$currencySymbol${activity.cost.toInt()}" else "Free",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SeaBlue
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
