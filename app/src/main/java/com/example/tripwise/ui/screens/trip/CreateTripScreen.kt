package com.example.tripwise.ui.screens.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tripwise.data.AppState
import com.example.tripwise.data.BudgetBreakdown
import com.example.tripwise.data.TravelActivity
import com.example.tripwise.data.Trip
import com.example.tripwise.data.TripStatus
import com.example.tripwise.ui.components.TripWiseFilterChip
import com.example.tripwise.ui.components.TripWisePrimaryButton
import com.example.tripwise.ui.components.TripWiseTextField
import com.example.tripwise.ui.theme.AzureMist
import com.example.tripwise.ui.theme.DuskBlue
import com.example.tripwise.ui.theme.SeaBlue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateTripScreen(
    appState: AppState,
    onBackClick: () -> Unit,
    onTripCreated: (String) -> Unit
) {
    var destinationName by remember { mutableStateOf(appState.selectedDestination.name) }
    var tripName by remember { mutableStateOf("My ${appState.selectedDestination.location} Holiday") }
    var startDate by remember { mutableStateOf("2026-11-01") }
    var endDate by remember { mutableStateOf("2026-11-07") }
    var travellersCount by remember { mutableIntStateOf(2) }
    var totalBudgetInput by remember { mutableStateOf("45000") }

    val currency = appState.currencySymbol

    // 7 Detailed Budget Categories
    var transportInput by remember { mutableStateOf("10000") }
    var accommodationInput by remember { mutableStateOf("15000") }
    var foodInput by remember { mutableStateOf("8000") }
    var activitiesInput by remember { mutableStateOf("6000") }
    var localTravelInput by remember { mutableStateOf("3000") }
    var shoppingInput by remember { mutableStateOf("2000") }
    var miscInput by remember { mutableStateOf("1000") }

    val interestOptions = listOf("Nature", "Culture", "Food", "Heritage", "Shopping", "Adventure")
    val selectedInterests = remember { mutableStateListOf("Nature", "Culture", "Food") }

    val styleOptions = listOf("Backpacker", "Balanced", "Luxury", "Adventure")
    var selectedStyle by remember { mutableStateOf("Balanced") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun submitNewTrip() {
        if (tripName.isBlank() || destinationName.isBlank()) {
            errorMessage = "Please enter both Trip Name and Destination"
            return
        }
        val totalBudget = totalBudgetInput.toDoubleOrNull() ?: 45000.0
        val newTripId = "t_${System.currentTimeMillis()}"

        val tCost = transportInput.toDoubleOrNull() ?: (totalBudget * 0.22)
        val aCost = accommodationInput.toDoubleOrNull() ?: (totalBudget * 0.33)
        val fCost = foodInput.toDoubleOrNull() ?: (totalBudget * 0.18)
        val actCost = activitiesInput.toDoubleOrNull() ?: (totalBudget * 0.13)
        val localCost = localTravelInput.toDoubleOrNull() ?: (totalBudget * 0.07)
        val shopCost = shoppingInput.toDoubleOrNull() ?: (totalBudget * 0.04)
        val miscCost = miscInput.toDoubleOrNull() ?: (totalBudget * 0.03)

        val newTrip = Trip(
            id = newTripId,
            tripName = tripName,
            destinationName = destinationName,
            startDateString = startDate,
            endDateString = endDate,
            travellersCount = travellersCount,
            totalBudgetLimit = totalBudget,
            budgetBreakdown = BudgetBreakdown(
                transport = tCost,
                accommodation = aCost,
                food = fCost,
                activities = actCost,
                miscellaneous = miscCost + localCost + shopCost
            ),
            interests = selectedInterests.toList(),
            travelStyle = selectedStyle,
            status = TripStatus.UPCOMING,
            activities = mutableListOf(
                TravelActivity(
                    id = "act_${System.currentTimeMillis()}_1",
                    dayIndex = 0,
                    title = "Arrival & Hotel Check-in",
                    location = destinationName,
                    description = "Arrive at hotel, unpack, and settle into the destination.",
                    time = "14:00",
                    duration = "2.0 hrs",
                    cost = 0.0,
                    isCompleted = false
                ),
                TravelActivity(
                    id = "act_${System.currentTimeMillis()}_2",
                    dayIndex = 0,
                    title = "Welcome Regional Dinner",
                    location = "$destinationName Hub",
                    description = "Taste authentic regional delicacies for dinner.",
                    time = "19:30",
                    duration = "2.0 hrs",
                    cost = fCost / 5,
                    isCompleted = false
                )
            )
        )

        appState.addTrip(newTrip)
        onTripCreated(newTripId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzureMist)
    ) {
        TopAppBar(
            title = { Text("Plan Asian Trip & Budget", fontWeight = FontWeight.Bold, color = DuskBlue) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DuskBlue)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AzureMist)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TripWiseTextField(
                value = tripName,
                onValueChange = { tripName = it },
                label = "Trip Name",
                placeholder = "e.g., Autumn in Japan",
                leadingIcon = Icons.Default.CardTravel
            )

            Spacer(modifier = Modifier.height(14.dp))

            TripWiseTextField(
                value = destinationName,
                onValueChange = { destinationName = it },
                label = "Destination (Asia)",
                placeholder = "e.g., Munnar, Kerala or Tokyo, Japan",
                leadingIcon = Icons.Default.LocationOn
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Dates
            Row(modifier = Modifier.fillMaxWidth()) {
                TripWiseTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = "Start Date",
                    leadingIcon = Icons.Default.DateRange,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                TripWiseTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = "End Date",
                    leadingIcon = Icons.Default.DateRange,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Travellers Counter
            Text(text = "Number of Travellers", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (travellersCount > 1) travellersCount-- },
                    modifier = Modifier
                        .background(SeaBlue.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .size(44.dp)
                ) {
                    Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SeaBlue)
                }

                Text(
                    text = "$travellersCount ${if (travellersCount == 1) "Traveller" else "Travellers"}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuskBlue,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                IconButton(
                    onClick = { travellersCount++ },
                    modifier = Modifier
                        .background(SeaBlue.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .size(44.dp)
                ) {
                    Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SeaBlue)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Total Budget Input First
            Text(text = "What is your total budget for this trip? (${appState.currencySymbol})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
            Spacer(modifier = Modifier.height(8.dp))

            TripWiseTextField(
                value = totalBudgetInput,
                onValueChange = { totalBudgetInput = it },
                label = "Total Budget Amount",
                leadingIcon = Icons.Default.AttachMoney
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Budget Allocation across 7 Detailed Categories
            Text(text = "Budget Allocation (Customize Categories)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Distribute your ${appState.currencySymbol}${totalBudgetInput} across categories:", fontSize = 13.sp, color = DuskBlue.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(12.dp))

            Row {
                TripWiseTextField(value = transportInput, onValueChange = { transportInput = it }, label = "Transportation", modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                TripWiseTextField(value = accommodationInput, onValueChange = { accommodationInput = it }, label = "Accommodation", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row {
                TripWiseTextField(value = foodInput, onValueChange = { foodInput = it }, label = "Food & Dining", modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                TripWiseTextField(value = activitiesInput, onValueChange = { activitiesInput = it }, label = "Activities & Tours", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row {
                TripWiseTextField(value = localTravelInput, onValueChange = { localTravelInput = it }, label = "Local Travel", modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                TripWiseTextField(value = shoppingInput, onValueChange = { shoppingInput = it }, label = "Shopping", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            TripWiseTextField(value = miscInput, onValueChange = { miscInput = it }, label = "Emergency / Miscellaneous")

            Spacer(modifier = Modifier.height(24.dp))

            // Interests
            Text(text = "Trip Interests", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                interestOptions.forEach { interest ->
                    val isSelected = selectedInterests.contains(interest)
                    TripWiseFilterChip(
                        text = interest,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelected) selectedInterests.remove(interest)
                            else selectedInterests.add(interest)
                        }
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(36.dp))

            TripWisePrimaryButton(
                text = "Create Itinerary & Budget",
                onClick = { submitNewTrip() }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
