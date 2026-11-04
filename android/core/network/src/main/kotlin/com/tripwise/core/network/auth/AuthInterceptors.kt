package com.tripwise.core.network.auth

import com.tripwise.core.network.api.AuthApi
import com.tripwise.core.network.dto.RefreshRequest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

private const val AUTHORIZATION = "Authorization"
private fun Request.withBearer(token: String) = newBuilder().header(AUTHORIZATION, "Bearer $token").build()

/** Adds the current access token to every authenticated request. */
@Singleton
class AuthInterceptor @Inject constructor(private val tokenStore: TokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // OkHttp interceptors run on its own worker threads, so blocking here is safe.
        val token = runBlocking { tokenStore.get() }?.accessToken
        val request = chain.request()
        return chain.proceed(if (token != null && request.header(AUTHORIZATION) == null) request.withBearer(token) else request)
    }
}

/**
 * On 401: refreshes the session once (single-flight across concurrent requests) and retries.
 * If the server rejects the refresh token, tokens are cleared and [SessionEvents.expired] fires.
 * Network failures during refresh keep the tokens (the user may just be offline).
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    // Provider breaks the cycle: AuthApi's client must not use this authenticator.
    private val authApi: Provider<AuthApi>,
    private val sessionEvents: SessionEvents,
) : Authenticator {
    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header(AUTHORIZATION) == null) return null // wasn't an authenticated call
        if (response.priorResponse != null) return null // already retried once; give up
        val failedToken = response.request.header(AUTHORIZATION)?.removePrefix("Bearer ")

        return runBlocking {
            mutex.withLock {
                val current = tokenStore.get() ?: return@withLock null
                // Another request refreshed while we waited: just retry with the new token.
                if (current.accessToken != failedToken) return@withLock response.request.withBearer(current.accessToken)

                try {
                    val refreshed = authApi.get().refresh(RefreshRequest(current.refreshToken)).tokens
                    val tokens = AuthTokens(refreshed.accessToken, refreshed.refreshToken)
                    tokenStore.save(tokens)
                    response.request.withBearer(tokens.accessToken)
                } catch (e: HttpException) {
                    if (e.code() == 401 || e.code() == 400) {
                        tokenStore.clear()
                        sessionEvents.notifyExpired()
                    }
                    null
                } catch (e: java.io.IOException) {
                    null
                }
            }
        }
    }
}
