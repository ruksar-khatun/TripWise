package com.tripwise.core.network.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

data class AuthTokens(val accessToken: String, val refreshToken: String)

/**
 * Secure persistence for the current session's tokens. Implemented in :core:datastore
 * (Tink AEAD + Android Keystore); faked in tests.
 */
interface TokenStore {
    suspend fun get(): AuthTokens?
    suspend fun save(tokens: AuthTokens)
    suspend fun clear()
}

/** Raised by the network layer when the server rejects the refresh token; the session must end. */
@Singleton
class SessionEvents @Inject constructor() {
    private val _expired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val expired: SharedFlow<Unit> = _expired.asSharedFlow()
    fun notifyExpired() { _expired.tryEmit(Unit) }
}
