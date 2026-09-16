package com.example.tripwise.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
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
fun LoginScreen(
    appState: AppState? = null,
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var showForgotDialog by remember { mutableStateOf(false) }
    var resetEmailInput by remember { mutableStateOf("") }
    var resetSuccessMessage by remember { mutableStateOf(false) }

    fun validateAndLogin() {
        var isValid = true
        if (email.isBlank() || !email.contains("@")) {
            emailError = "Please enter a valid email address"
            isValid = false
        } else emailError = null

        if (password.isBlank()) {
            passwordError = "Password cannot be empty"
            isValid = false
        } else passwordError = null

        if (isValid) {
            val username = email.substringBefore("@").replaceFirstChar { it.uppercase() } + " Traveler"
            appState?.updateFullProfile(
                name = username,
                email = email,
                phone = appState.userProfile.phone,
                travelStyle = appState.userProfile.travelStyle,
                companion = appState.userProfile.companion,
                budgetRange = appState.userProfile.budgetRange
            )
            onLoginSuccess()
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            confirmButton = {
                Button(
                    onClick = { resetSuccessMessage = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SeaBlue)
                ) {
                    Text("Send Reset Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Cancel", color = DuskBlue)
                }
            },
            title = { Text("Reset Password", fontWeight = FontWeight.Bold, color = DuskBlue) },
            text = {
                Column {
                    Text("Enter your account email address to receive password reset instructions.", color = DuskBlue.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(12.dp))
                    TripWiseTextField(
                        value = resetEmailInput,
                        onValueChange = { resetEmailInput = it },
                        label = "Reset Email",
                        leadingIcon = Icons.Default.Email
                    )
                    if (resetSuccessMessage) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Reset link sent! Check your inbox.", color = SeaBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        )
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
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome Back",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = DuskBlue
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sign in to access your planned trips and saved places",
            fontSize = 15.sp,
            color = DuskBlue.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(40.dp))

        TripWiseTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            leadingIcon = Icons.Default.Email,
            errorMessage = emailError
        )

        Spacer(modifier = Modifier.height(16.dp))

        TripWiseTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isPassword = true,
            leadingIcon = Icons.Default.Lock,
            errorMessage = passwordError
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            TextButton(onClick = {
                resetEmailInput = email
                resetSuccessMessage = false
                showForgotDialog = true
            }) {
                Text(text = "Forgot Password?", color = SeaBlue, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        TripWisePrimaryButton(
            text = "Log In",
            onClick = { validateAndLogin() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account? ",
                color = DuskBlue.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            TextButton(onClick = onSignUpClick) {
                Text(
                    text = "Sign Up",
                    color = SeaBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
