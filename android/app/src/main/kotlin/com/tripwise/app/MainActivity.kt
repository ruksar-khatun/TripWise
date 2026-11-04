package com.tripwise.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tripwise.app.ui.TripWiseRoot
import com.tripwise.core.common.network.NetworkMonitor
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.domain.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var networkMonitor: NetworkMonitor
    @Inject lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            // Theme mode (Light/Dark/System) will come from user preferences once that setting is approved.
            TripWiseTheme {
                TripWiseRoot(authRepository, networkMonitor)
            }
        }
    }
}
