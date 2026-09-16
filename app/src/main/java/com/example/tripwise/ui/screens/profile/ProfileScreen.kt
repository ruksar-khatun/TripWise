package com.example.tripwise.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tripwise.R
import com.example.tripwise.data.AppState
import com.example.tripwise.ui.components.TripWiseCard
import com.example.tripwise.ui.components.TripWisePrimaryButton
import com.example.tripwise.ui.components.TripWiseTextField
import com.example.tripwise.ui.theme.AzureMist
import com.example.tripwise.ui.theme.DuskBlue
import com.example.tripwise.ui.theme.SeaBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    appState: AppState,
    onEditPreferencesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit
) {
    val user = appState.userProfile
    var showPhotoPicker by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    var editName by remember { mutableStateOf(user.name) }
    var editEmail by remember { mutableStateOf(user.email) }
    var editPhone by remember { mutableStateOf(user.phone) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            appState.updateProfileAvatarUri(uri.toString())
        }
    }

    val avatarOptions = listOf(
        R.drawable.img_15,
        R.drawable.img,
        R.drawable.img_3,
        R.drawable.img_6,
        R.drawable.img_9,
        R.drawable.img_12
    )

    if (showPhotoPicker) {
        AlertDialog(
            onDismissRequest = { showPhotoPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showPhotoPicker = false
                    imagePickerLauncher.launch("image/*")
                }) {
                    Text("Upload from Device", color = SeaBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhotoPicker = false }) {
                    Text("Cancel", color = DuskBlue)
                }
            },
            title = { Text("Choose Profile Photo", fontWeight = FontWeight.Bold, color = DuskBlue) },
            text = {
                Column {
                    Text("Select preset avatar or pick from device storage:", fontSize = 13.sp, color = DuskBlue.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(avatarOptions) { resId ->
                            Box(
                                modifier = Modifier
                                    .size(65.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        appState.updateProfileAvatar(resId)
                                        showPhotoPicker = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        )
    }

    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            confirmButton = {
                TripWisePrimaryButton(
                    text = "Save Changes",
                    onClick = {
                        appState.updateFullProfile(
                            name = editName,
                            email = editEmail,
                            phone = editPhone,
                            travelStyle = user.travelStyle,
                            companion = user.companion,
                            budgetRange = user.budgetRange
                        )
                        showEditProfileDialog = false
                    },
                    modifier = Modifier.width(160.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = DuskBlue)
                }
            },
            title = { Text("Edit Profile Details", fontWeight = FontWeight.Bold, color = DuskBlue) },
            text = {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    TripWiseTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = "Full Name",
                        leadingIcon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TripWiseTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = "Email Address",
                        leadingIcon = Icons.Default.Email
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TripWiseTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = "Phone Number",
                        leadingIcon = Icons.Default.Phone
                    )
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
            title = { Text("User Profile", fontWeight = FontWeight.Bold, color = DuskBlue) },
            actions = {
                IconButton(onClick = {
                    editName = user.name
                    editEmail = user.email
                    editPhone = user.phone
                    showEditProfileDialog = true
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = DuskBlue)
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = DuskBlue)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AzureMist)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar & Name with edit photo overlay
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .clickable { showPhotoPicker = true },
                contentAlignment = Alignment.BottomEnd
            ) {
                if (user.avatarUri != null) {
                    AsyncImage(
                        model = user.avatarUri,
                        contentDescription = user.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = user.avatarRes),
                        contentDescription = user.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(SeaBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Change Photo", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = user.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DuskBlue
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.email,
                fontSize = 14.sp,
                color = DuskBlue.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = user.phone,
                fontSize = 13.sp,
                color = SeaBlue,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Travel Statistics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(title = "Trips Planned", value = "${appState.trips.size}", modifier = Modifier.weight(1f))
                StatCard(title = "Countries", value = "${user.countriesVisited}", modifier = Modifier.weight(1f))
                StatCard(title = "Saved Places", value = "${user.savedCount}", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preferences Summary Card
            TripWiseCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Travel Preferences", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DuskBlue)
                        TextButton(onClick = onEditPreferencesClick) {
                            Text(text = "Edit", color = SeaBlue, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Travel Style: ${user.travelStyle}", fontSize = 14.sp, color = DuskBlue.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Preferred Companion: ${user.companion}", fontSize = 14.sp, color = DuskBlue.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Budget Range: ${user.budgetRange}", fontSize = 14.sp, color = DuskBlue.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Interests: ${user.interests.joinToString(", ")}", fontSize = 14.sp, color = DuskBlue.copy(alpha = 0.8f))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            ProfileMenuRow(title = "Edit Profile Details", icon = Icons.Default.Edit, onClick = {
                editName = user.name
                editEmail = user.email
                editPhone = user.phone
                showEditProfileDialog = true
            })
            Spacer(modifier = Modifier.height(12.dp))
            ProfileMenuRow(title = "Account Settings", icon = Icons.Default.Settings, onClick = onSettingsClick)
            Spacer(modifier = Modifier.height(12.dp))
            ProfileMenuRow(title = "Log Out", icon = Icons.AutoMirrored.Filled.Logout, tint = Color.Red, onClick = onLogoutClick)
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    TripWiseCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SeaBlue)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 12.sp, color = DuskBlue.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun ProfileMenuRow(title: String, icon: ImageVector, tint: Color = DuskBlue, onClick: () -> Unit) {
    TripWiseCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = tint, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = tint)
        }
    }
}
