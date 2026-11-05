package com.tripwise.core.data.repository

import com.tripwise.core.common.coroutines.ApplicationScope
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.common.result.map
import com.tripwise.core.common.result.onSuccess
import com.tripwise.core.data.mapper.toModel
import com.tripwise.core.datastore.ProfileCache
import com.tripwise.core.domain.repository.AuthRepository
import com.tripwise.core.domain.repository.UserRepository
import com.tripwise.core.model.SessionState
import com.tripwise.core.model.UserProfile
import com.tripwise.core.model.guestProfile
import com.tripwise.core.model.isGuest
import com.tripwise.core.network.auth.AuthTokens
import com.tripwise.core.network.auth.SessionEvents
import com.tripwise.core.network.auth.TokenStore
import com.tripwise.core.network.datasource.AuthNetworkDataSource
import com.tripwise.core.network.dto.AuthResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the session lifecycle:
 *  - start-up: stored tokens + cached profile → SignedIn immediately (works offline), then refresh /me
 *  - sign-in/up: store tokens securely, cache profile
 *  - sign-out / expiry / account deletion: clear tokens, profile and all local user data
 */
@Singleton
internal class DefaultAuthRepository @Inject constructor(
    private val network: AuthNetworkDataSource,
    private val tokenStore: TokenStore,
    private val profileCache: ProfileCache,
    private val localData: LocalUserData,
    sessionEvents: SessionEvents,
    @ApplicationScope private val scope: CoroutineScope,
) : AuthRepository {

    private val _session = MutableStateFlow<SessionState>(SessionState.Unknown)
    override val session: StateFlow<SessionState> = _session.asStateFlow()
    private val transition = Mutex()

    init {
        scope.launch { restore() }
        scope.launch { sessionEvents.expired.collect { endSession(expired = true, notifyServer = false) } }
    }

    private suspend fun restore() {
        val tokens = tokenStore.get()
        val cached = profileCache.get()
        if (tokens != null && cached != null) {
            _session.value = SessionState.SignedIn(cached)
            network.me().onSuccess { dto -> updateProfile(dto.toModel()) }
        } else if (tokens == null && cached?.isGuest == true) {
            _session.value = SessionState.SignedIn(cached) // guest mode: works with no server at all
        } else {
            if (tokens != null || cached != null) clearLocal() // half-written state: start clean
            _session.value = SessionState.SignedOut()
        }
    }

    override suspend fun signIn(email: String, password: String): Outcome<Unit> =
        establish(network.login(email.trim(), password))

    override suspend fun signUp(displayName: String, email: String, password: String): Outcome<Unit> =
        establish(network.register(email.trim(), password, displayName.trim()))

    override suspend fun signOut() = endSession(expired = false, notifyServer = true)

    override suspend fun continueAsGuest() {
        android.util.Log.i(TAG, "guest: requested")
        transition.withLock {
            android.util.Log.i(TAG, "guest: lock held, clearing tokens")
            tokenStore.clear()
            android.util.Log.i(TAG, "guest: saving profile")
            val guest = guestProfile()
            profileCache.save(guest)
            _session.value = SessionState.SignedIn(guest)
            android.util.Log.i(TAG, "guest: signed in")
        }
    }

    override suspend fun leaveGuestMode() = transition.withLock {
        if (profileCache.get()?.isGuest == true) profileCache.clear() // local trips are kept on purpose
        _session.value = SessionState.SignedOut()
    }

    override suspend fun deleteAccount(password: String): Outcome<Unit> =
        network.deleteAccount(password).onSuccess { endSession(expired = false, notifyServer = false) }

    private suspend fun establish(result: Outcome<AuthResponse>): Outcome<Unit> = transition.withLock {
        result.onSuccess { response ->
            val user = response.user.toModel()
            // A different account than last time: never show the previous user's local data.
            // Coming from guest mode (or a fresh install) keeps the trips made on this phone.
            val previous = profileCache.get()
            if (previous != null && !previous.isGuest && previous.id != user.id) localData.clear()
            tokenStore.save(AuthTokens(response.tokens.accessToken, response.tokens.refreshToken))
            profileCache.save(user)
            _session.value = SessionState.SignedIn(user)
        }.map { }
    }

    private suspend fun updateProfile(user: UserProfile) = transition.withLock {
        if (_session.value is SessionState.SignedIn) {
            profileCache.save(user)
            _session.value = SessionState.SignedIn(user)
        }
    }

    private suspend fun endSession(expired: Boolean, notifyServer: Boolean) = transition.withLock {
        if (notifyServer) {
            // Best effort: revoke on the server, but never block sign-out on the network.
            tokenStore.get()?.let { withTimeoutOrNull(LOGOUT_TIMEOUT_MS) { network.logout(it.refreshToken) } }
        }
        clearLocal()
        _session.value = SessionState.SignedOut(expired)
    }

    /** Order matters: user data goes before the profile, so "no cached profile" always means "no previous user's data". */
    private suspend fun clearLocal() {
        tokenStore.clear()
        localData.clear()
        profileCache.clear()
    }

    private companion object {
        const val LOGOUT_TIMEOUT_MS = 5_000L
        const val TAG = "Session"
    }
}

internal class SessionUserRepository @Inject constructor(private val auth: AuthRepository) : UserRepository {
    override fun observeProfile(): Flow<UserProfile> =
        auth.session.filterIsInstance<SessionState.SignedIn>().map { it.user }
}
