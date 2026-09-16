package com.example.tripwise.ui.screens.trip

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.tripwise.R
import com.example.tripwise.data.AppState
import com.example.tripwise.ui.components.TripWiseCard
import com.example.tripwise.ui.components.TripWisePrimaryButton
import com.example.tripwise.ui.theme.AzureMist
import com.example.tripwise.ui.theme.DuskBlue
import com.example.tripwise.ui.theme.SeaBlue

@Composable
fun ActivityDetailsScreen(
    activityId: String,
    appState: AppState,
    onBackClick: () -> Unit
) {
    val activity = appState.selectedTrip.activities.firstOrNull { it.id == activityId }
        ?: appState.selectedActivity
        ?: com.example.tripwise.data.TravelActivity(
            id = activityId,
            dayIndex = 0,
            title = "Explore Historic Landmark",
            location = appState.selectedTrip.destinationName,
            description = "Experience majestic sightseeing and guided cultural tour.",
            time = "10:00 AM",
            duration = "2.5 hrs",
            cost = 45.0,
            imageRes = R.drawable.img_1,
            isCompleted = false
        )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AzureMist)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 90.dp)
        ) {
            // Hero Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                Image(
                    painter = painterResource(id = activity.imageRes ?: R.drawable.img),
                    contentDescription = activity.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(20.dp)
                        .size(42.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DuskBlue)
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activity.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuskBlue,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (activity.isCompleted) SeaBlue.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (activity.isCompleted) "Completed" else "Pending",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activity.isCompleted) SeaBlue else Color.Gray,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TripWiseCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = SeaBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = activity.location, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DuskBlue)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = DuskBlue.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = SeaBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "${activity.time} (${activity.duration})", fontSize = 14.sp, color = DuskBlue.copy(alpha = 0.8f))
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = SeaBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = if (activity.cost > 0) "${appState.currencySymbol}${activity.cost.toInt()}" else "Free", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SeaBlue)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Description",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuskBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = activity.description,
                    fontSize = 14.sp,
                    color = DuskBlue.copy(alpha = 0.8f),
                    lineHeight = 22.sp
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shadowElevation = 16.dp,
            color = Color.White
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding()
            ) {
                TripWisePrimaryButton(
                    text = if (activity.isCompleted) "Mark as Pending" else "Mark as Completed",
                    onClick = {
                        appState.toggleActivityCompleted(activity.id)
                    }
                )
            }
        }
    }
}
