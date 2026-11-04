package com.tripwise.core.network

import com.tripwise.core.network.api.AuthApi
import com.tripwise.core.network.auth.AuthInterceptor
import com.tripwise.core.network.auth.AuthTokens
import com.tripwise.core.network.auth.SessionEvents
import com.tripwise.core.network.auth.TokenAuthenticator
import com.tripwise.core.network.auth.TokenStore
import com.tripwise.core.network.di.NetworkModule
import com.tripwise.core.network.di.buildOkHttpClient
import com.tripwise.core.network.di.buildRetrofit
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

private class InMemoryTokenStore(var tokens: AuthTokens?) : TokenStore {
    override suspend fun get() = tokens
    override suspend fun save(tokens: AuthTokens) { this.tokens = tokens }
    override suspend fun clear() { tokens = null }
}

class TokenAuthenticatorTest {
    private val server = MockWebServer()
    private val refreshCalls = AtomicInteger()
    private lateinit var store: InMemoryTokenStore
    private lateinit var events: SessionEvents
    private lateinit var client: okhttp3.OkHttpClient

    /** Server behaviour: "fresh" access token is valid; refresh returns it unless [refreshFails]. */
    @Volatile private var refreshStatus = 200

    private fun json(code: Int, body: String) =
        MockResponse.Builder().code(code).addHeader("Content-Type", "application/json").body(body).build()

    @Before fun setUp() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when (request.url.encodedPath) {
                "/api/v1/auth/refresh" -> {
                    refreshCalls.incrementAndGet()
                    Thread.sleep(100) // widen the race window for the concurrency test
                    if (refreshStatus == 200) {
                        json(200, """{"user":{"id":"u","email":"e","displayName":"n","homeCurrency":"INR","createdAt":"x"},
                            "tokens":{"accessToken":"fresh","accessTokenExpiresIn":900,"refreshToken":"r2","refreshTokenExpiresAt":"x"}}""")
                    } else {
                        json(refreshStatus, """{"error":{"code":"UNAUTHORIZED","message":"expired"}}""")
                    }
                }
                else -> if (request.headers["Authorization"] == "Bearer fresh") json(200, "{}") else json(401, "{}")
            }
        }
        server.start()
        store = InMemoryTokenStore(AuthTokens("stale", "r1"))
        events = SessionEvents()
        val config = NetworkConfig(server.url("/").toString(), enableHttpLogging = false, userAgent = "test")
        val publicClient = buildOkHttpClient(config)
        val authApi = buildRetrofit(config.baseUrl, NetworkModule.provideJson(), publicClient).create(AuthApi::class.java)
        client = publicClient.newBuilder()
            .addInterceptor(AuthInterceptor(store))
            .authenticator(TokenAuthenticator(store, { authApi }, events))
            .build()
    }

    @After fun tearDown() = server.close()

    private fun get() = client.newCall(Request.Builder().url(server.url("/api/v1/me")).build()).execute().use { it.code }

    @Test fun `401 triggers one refresh, saves rotated tokens and retries`() {
        assertEquals(200, get())
        assertEquals(1, refreshCalls.get())
        assertEquals(AuthTokens("fresh", "r2"), store.tokens)
    }

    @Test fun `concurrent 401s share a single refresh`() = runBlocking {
        val codes = (1..5).map { async(kotlinx.coroutines.Dispatchers.IO) { get() } }.awaitAll()
        assertEquals(List(5) { 200 }, codes)
        assertEquals(1, refreshCalls.get())
    }

    @Test fun `rejected refresh clears tokens and signals session expiry`() = runBlocking {
        refreshStatus = 401
        val expired = async { withTimeoutOrNull(5_000) { events.expired.first() } }
        kotlinx.coroutines.yield()
        assertEquals(401, kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { get() })
        assertNull(store.tokens)
        assertNotNull(expired.await())
    }

    @Test fun `server error during refresh keeps tokens (may be transient)`() {
        refreshStatus = 503
        assertEquals(401, get())
        assertEquals(AuthTokens("stale", "r1"), store.tokens)
    }

    @Test fun `no stored tokens means no auth header and no refresh`() {
        store.tokens = null
        assertEquals(401, get())
        assertEquals(0, refreshCalls.get())
    }
}
