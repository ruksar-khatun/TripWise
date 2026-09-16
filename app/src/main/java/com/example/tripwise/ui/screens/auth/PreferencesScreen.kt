package com.example.tripwise.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tripwise.data.AppState
import com.example.tripwise.ui.components.TripWiseFilterChip
import com.example.tripwise.ui.components.TripWisePrimaryButton
import com.example.tripwise.ui.theme.AzureMist
import com.example.tripwise.ui.theme.DuskBlue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreferencesScreen(
    appState: AppState? = null,
    onComplete: () -> Unit
) {
    val allInterests = listOf("Nature", "Culture", "Food", "Beach", "Heritage", "Photography", "Adventure", "Shopping")
    val selectedInterests = remember { mutableStateListOf("Nature", "Culture", "Food") }

    val styles = listOf("Backpacker", "Adventure", "Balanced", "Luxury")
    var selectedStyle by remember { mutableStateOf("Adventure") }

    val companions = listOf("Solo", "Couple", "Friends", "Family")
    var selectedCompanion by remember { mutableStateOf("Friends") }

    val budgetRanges = listOf("Budget", "Mid-range", "Luxury")
    var selectedBudget by remember { mutableStateOf("Mid-range") }

    fun savePreferences() {
        appState?.updateUserPreferences(
            interests = selectedInterests.toList(),
            style = selectedStyle,
            companion = selectedCompanion,
            budgetRange = selectedBudget
        )
        onComplete()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzureMist)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Travel Preferences",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DuskBlue
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Help us personalize your trip recommendations and itinerary suggestions",
            fontSize = 15.sp,
            color = DuskBlue.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Interests
        Text(
            text = "What interests you most?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DuskBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allInterests.forEach { interest ->
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

        Spacer(modifier = Modifier.height(28.dp))

        // Travel Style
        Text(
            text = "Travel Style",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DuskBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            styles.forEach { style ->
                TripWiseFilterChip(
                    text = style,
                    isSelected = selectedStyle == style,
                    onClick = { selectedStyle = style }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Companions
        Text(
            text = "Typical Companions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DuskBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            companions.forEach { companion ->
                TripWiseFilterChip(
                    text = companion,
                    isSelected = selectedCompanion == companion,
                    onClick = { selectedCompanion = companion }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Budget
        Text(
            text = "Preferred Budget",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DuskBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            budgetRanges.forEach { budget ->
                TripWiseFilterChip(
                    text = budget,
                    isSelected = selectedBudget == budget,
                    onClick = { selectedBudget = budget }
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        TripWisePrimaryButton(
            text = "Complete & Continue",
            onClick = { savePreferences() }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
