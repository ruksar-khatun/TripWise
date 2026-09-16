package com.example.tripwise.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tripwise.data.AppState
import com.example.tripwise.ui.components.TripWisePrimaryButton
import com.example.tripwise.ui.components.TripWiseTextField
import com.example.tripwise.ui.theme.AzureMist
import com.example.tripwise.ui.theme.DuskBlue
import com.example.tripwise.ui.theme.SeaBlue

@Composable
fun SignUpScreen(
    appState: AppState? = null,
    onSignUpSuccess: () -> Unit,
    onLoginClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    fun validateAndSubmit() {
        var isValid = true
        if (name.isBlank()) {
            nameError = "Full name is required"
            isValid = false
        } else nameError = null

        if (email.isBlank() || !email.contains("@")) {
            emailError = "Valid email address is required"
            isValid = false
        } else emailError = null

        if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            isValid = false
        } else passwordError = null

        if (confirmPassword != password) {
            confirmError = "Passwords do not match"
            isValid = false
        } else confirmError = null

        if (isValid) {
            appState?.updateFullProfile(
                name = name,
                email = email,
                phone = appState.userProfile.phone,
                travelStyle = appState.userProfile.travelStyle,
                companion = appState.userProfile.companion,
                budgetRange = appState.userProfile.budgetRange
            )
            onSignUpSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzureMist)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Create Account",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = DuskBlue
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Join TripWise to plan your dream adventures",
            fontSize = 15.sp,
            color = DuskBlue.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(36.dp))

        TripWiseTextField(
            value = name,
            onValueChange = { name = it },
            label = "Full Name",
            leadingIcon = Icons.Default.Person,
            errorMessage = nameError
        )

        Spacer(modifier = Modifier.height(14.dp))

        TripWiseTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            leadingIcon = Icons.Default.Email,
            errorMessage = emailError
        )

        Spacer(modifier = Modifier.height(14.dp))

        TripWiseTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isPassword = true,
            leadingIcon = Icons.Default.Lock,
            errorMessage = passwordError
        )

        Spacer(modifier = Modifier.height(14.dp))

        TripWiseTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm Password",
            isPassword = true,
            leadingIcon = Icons.Default.Lock,
            errorMessage = confirmError
        )

        Spacer(modifier = Modifier.height(32.dp))

        TripWisePrimaryButton(
            text = "Sign Up",
            onClick = { validateAndSubmit() }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                color = DuskBlue.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            TextButton(onClick = onLoginClick) {
                Text(
                    text = "Log In",
                    color = SeaBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
