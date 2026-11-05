package com.tripwise.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.tripwise.core.common.network.NetworkMonitor
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.domain.repository.AuthRepository
import com.tripwise.core.model.SessionState
import com.tripwise.feature.auth.navigation.SignInRoute
import com.tripwise.feature.auth.navigation.authGraph

/**
 * Session gate. Signed-out and signed-in UIs are separate NavHosts, so switching session state
 * always starts from a clean back stack (no "back" into another user's screens).
 */
@Composable
fun TripWiseRoot(authRepository: AuthRepository, networkMonitor: NetworkMonitor) {
    val session by authRepository.session.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(session) { android.util.Log.i("Session", "ui: ${session::class.simpleName}") }
    AnimatedContent(
        targetState = session,
        contentKey = { it::class to (it as? SessionState.SignedIn)?.user?.id },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "session",
    ) { state ->
        when (state) {
            SessionState.Unknown -> Box(Modifier.fillMaxSize().background(TripWiseTheme.colors.background), Alignment.Center) {
                CircularProgressIndicator()
            }
            is SessionState.SignedOut -> {
                val nav = rememberNavController()
                NavHost(nav, startDestination = SignInRoute, modifier = Modifier.fillMaxSize()) {
                    authGraph(nav, sessionExpired = state.expired)
                }
            }
            is SessionState.SignedIn -> key(state.user.id) {
                TripWiseApp(rememberTripWiseAppState(networkMonitor))
            }
        }
    }
}
