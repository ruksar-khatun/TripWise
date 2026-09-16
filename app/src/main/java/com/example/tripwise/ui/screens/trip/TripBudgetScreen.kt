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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun TripBudgetScreen(
    tripId: String,
    appState: AppState,
    onBackClick: () -> Unit
) {
    appState.selectedTripId = tripId
    val trip = appState.selectedTrip
    val budget = trip.budgetBreakdown
    val currency = appState.currencySymbol

    val totalSpent = budget.transport + budget.accommodation + budget.food + budget.activities + budget.miscellaneous
    val remaining = trip.totalBudgetLimit - totalSpent
    val progress = (totalSpent / trip.totalBudgetLimit).coerceIn(0.0, 1.0).toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzureMist)
    ) {
        TopAppBar(
            title = { Text("Trip Budget Breakdown", fontWeight = FontWeight.Bold, color = DuskBlue) },
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
            // Summary Card
            TripWiseCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Budget", fontSize = 13.sp, color = DuskBlue.copy(alpha = 0.6f))
                            Text(text = "$currency${trip.totalBudgetLimit.toInt()}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Remaining", fontSize = 13.sp, color = DuskBlue.copy(alpha = 0.6f))
                            Text(text = "$currency${remaining.toInt()}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (remaining >= 0) SeaBlue else Color.Red)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = SeaBlue,
                        trackColor = SeaBlue.copy(alpha = 0.2f),
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Spent: $currency${totalSpent.toInt()} (${(progress * 100).toInt()}%)",
                        fontSize = 12.sp,
                        color = DuskBlue.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Expense Categories",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DuskBlue
            )

            Spacer(modifier = Modifier.height(12.dp))

            BudgetCategoryRow("Accommodation", budget.accommodation, Icons.Default.Hotel, totalSpent, currency)
            Spacer(modifier = Modifier.height(12.dp))
            BudgetCategoryRow("Transportation", budget.transport, Icons.Default.Flight, totalSpent, currency)
            Spacer(modifier = Modifier.height(12.dp))
            BudgetCategoryRow("Food & Dining", budget.food, Icons.Default.Restaurant, totalSpent, currency)
            Spacer(modifier = Modifier.height(12.dp))
            BudgetCategoryRow("Activities & Tours", budget.activities, Icons.Default.LocalActivity, totalSpent, currency)
            Spacer(modifier = Modifier.height(12.dp))
            BudgetCategoryRow("Miscellaneous", budget.miscellaneous, Icons.Default.MoreHoriz, totalSpent, currency)
        }
    }
}

@Composable
fun BudgetCategoryRow(
    categoryName: String,
    amount: Double,
    icon: ImageVector,
    totalSpent: Double,
    currencySymbol: String
) {
    val percentage = if (totalSpent > 0) (amount / totalSpent).toFloat() else 0f

    TripWiseCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SeaBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = categoryName, tint = SeaBlue, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = categoryName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "${(percentage * 100).toInt()}% of total expenses", fontSize = 12.sp, color = DuskBlue.copy(alpha = 0.6f))
            }

            Text(
                text = "$currencySymbol${amount.toInt()}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DuskBlue
            )
        }
    }
}
