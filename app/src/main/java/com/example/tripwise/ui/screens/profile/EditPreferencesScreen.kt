package com.example.tripwise.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tripwise.data.AppState
import com.example.tripwise.ui.components.TripWiseFilterChip
import com.example.tripwise.ui.components.TripWisePrimaryButton
import com.example.tripwise.ui.theme.AzureMist
import com.example.tripwise.ui.theme.DuskBlue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditPreferencesScreen(
    appState: AppState,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val user = appState.userProfile
    val allInterests = listOf("Nature", "Culture", "Food", "Beach", "Heritage", "Photography", "Adventure", "Shopping")
    val selectedInterests = remember { mutableStateListOf(*user.interests.toTypedArray()) }

    val styles = listOf("Backpacker", "Adventure", "Balanced", "Luxury")
    var selectedStyle by remember { mutableStateOf(user.travelStyle) }

    val companions = listOf("Solo", "Couple", "Friends", "Family")
    var selectedCompanion by remember { mutableStateOf(user.companion) }

    val budgetRanges = listOf("Budget", "Mid-range", "Luxury")
    var selectedBudget by remember { mutableStateOf(user.budgetRange) }

    fun save() {
        appState.updateUserPreferences(
            interests = selectedInterests.toList(),
            style = selectedStyle,
            companion = selectedCompanion,
            budgetRange = selectedBudget
        )
        onSaveClick()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzureMist)
    ) {
        TopAppBar(
            title = { Text("Edit Preferences", fontWeight = FontWeight.Bold, color = DuskBlue) },
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "Interests", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
            Spacer(modifier = Modifier.height(10.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Travel Style", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
            Spacer(modifier = Modifier.height(10.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Typical Companion", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
            Spacer(modifier = Modifier.height(10.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Preferred Budget", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
            Spacer(modifier = Modifier.height(10.dp))

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
                text = "Save Changes",
                onClick = { save() }
            )
        }
    }
}
