package com.example.tripwise.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.example.tripwise.data.AsianCurrencies
import com.example.tripwise.ui.components.TripWiseCard
import com.example.tripwise.ui.theme.AzureMist
import com.example.tripwise.ui.theme.DuskBlue
import com.example.tripwise.ui.theme.SeaBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    appState: AppState,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var notifications by remember { mutableStateOf(appState.notificationsEnabled) }
    var language by remember { mutableStateOf(appState.selectedLanguage) }

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val themes = listOf("Light", "Dark", "System")
    val filteredCurrencies = AsianCurrencies.list.filter {
        it.country.contains(searchQuery, ignoreCase = true) ||
                it.currencyName.contains(searchQuery, ignoreCase = true) ||
                it.code.contains(searchQuery, ignoreCase = true)
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close", color = DuskBlue)
                }
            },
            title = { Text("Select Appearance Theme", fontWeight = FontWeight.Bold, color = DuskBlue) },
            text = {
                Column {
                    themes.forEach { t ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    appState.changeTheme(t)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = t, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DuskBlue)
                            if (appState.themeMode == t) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = SeaBlue)
                            }
                        }
                    }
                }
            }
        )
    }

    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Close", color = DuskBlue)
                }
            },
            title = { Text("Select Asian Currency", fontWeight = FontWeight.Bold, color = DuskBlue) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search country or code...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredCurrencies) { currInfo ->
                            val displayStr = "${currInfo.country} — ${currInfo.currencyName} (${currInfo.code} ${currInfo.symbol})"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        appState.changeCurrency(displayStr)
                                        showCurrencyDialog = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = currInfo.country, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                                    Text(text = "${currInfo.currencyName} • ${currInfo.code} (${currInfo.symbol})", fontSize = 12.sp, color = DuskBlue.copy(alpha = 0.7f))
                                }
                                if (appState.selectedCurrency == displayStr) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = SeaBlue)
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzureMist)
    ) {
        TopAppBar(
            title = { Text("Settings", fontWeight = FontWeight.Bold, color = DuskBlue) },
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
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Preferences", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Push Notifications", fontSize = 15.sp, color = DuskBlue)
                        Switch(
                            checked = notifications,
                            onCheckedChange = {
                                notifications = it
                                appState.notificationsEnabled = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = SeaBlue)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Appearance Theme", fontSize = 15.sp, color = DuskBlue)
                        TextButton(onClick = { showThemeDialog = true }) {
                            Text(text = appState.themeMode, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SeaBlue)
                        }
                    }
                }
            }

            TripWiseCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showCurrencyDialog = true }
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Regional & Currency", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Selected Currency", fontSize = 15.sp, color = DuskBlue)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = appState.selectedCurrency, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SeaBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = DuskBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Language", fontSize = 15.sp, color = DuskBlue)
                        Text(text = language, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SeaBlue)
                    }
                }
            }

            TripWiseCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onLogoutClick
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color.Red)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Log Out", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                }
            }
        }
    }
}
