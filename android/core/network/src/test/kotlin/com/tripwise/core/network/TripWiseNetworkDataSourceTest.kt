package com.tripwise.core.network

import com.tripwise.core.common.error.AiFailure
import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.network.api.TripWiseApi
import com.tripwise.core.network.datasource.RetrofitTripWiseNetworkDataSource
import com.tripwise.core.network.di.NetworkModule
import com.tripwise.core.network.di.buildOkHttpClient
import com.tripwise.core.network.di.buildRetrofit
import com.tripwise.core.network.dto.HealthDto
import com.tripwise.core.network.error.ApiErrorMapper
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TripWiseNetworkDataSourceTest {
    private val server = MockWebServer()
    private lateinit var dataSource: RetrofitTripWiseNetworkDataSource

    @Before fun setUp() {
        server.start()
        val json = NetworkModule.provideJson()
        val config = NetworkConfig(server.url("/").toString(), enableHttpLogging = false, userAgent = "TripWise-test")
        val api = buildRetrofit(config.baseUrl, json, buildOkHttpClient(config)).create(TripWiseApi::class.java)
        dataSource = RetrofitTripWiseNetworkDataSource(api, ApiErrorMapper(json))
    }

    @After fun tearDown() = server.close()

    private fun respond(code: Int, body: String) =
        server.enqueue(MockResponse.Builder().code(code).addHeader("Content-Type", "application/json").body(body).build())

    @Test fun `success is parsed and unknown fields ignored`() = runTest {
        respond(200, """{"status":"ok","service":"tripwise-backend","version":"0.1.0","extra":1}""")
        assertEquals(Outcome.Success(HealthDto("ok", "tripwise-backend", "0.1.0")), dataSource.health())
        val request = server.takeRequest()
        assertEquals("/api/v1/health", request.url.encodedPath)
        assertEquals("TripWise-test", request.headers["User-Agent"])
    }

    @Test fun `error envelope code wins over status`() = runTest {
        respond(400, """{"error":{"code":"VALIDATION_FAILED","message":"bad","details":{"name":"Required"}}}""")
        assertEquals(Outcome.Failure(AppError.Validation(mapOf("name" to "Required"))), dataSource.health())
    }

    @Test fun `ai codes are mapped`() = runTest {
        respond(503, """{"error":{"code":"AI_UNAVAILABLE","message":"down"}}""")
        assertEquals(Outcome.Failure(AppError.Ai(AiFailure.UNAVAILABLE)), dataSource.health())
    }

    @Test fun `status fallback when body is not an envelope`() = runTest {
        respond(503, "<html>oops</html>")
        assertEquals(Outcome.Failure(AppError.Server(503)), dataSource.health())
        respond(404, "")
        assertEquals(Outcome.Failure(AppError.NotFound), dataSource.health())
        respond(429, "{}")
        assertEquals(Outcome.Failure(AppError.RateLimited), dataSource.health())
    }

    @Test fun `malformed success body is Unknown`() = runTest {
        respond(200, """{"status":"ok"}""")
        val result = dataSource.health()
        assert(result is Outcome.Failure && result.error is AppError.Unknown)
    }

    @Test fun `unreachable server is Network`() = runTest {
        server.close() // connection refused → IOException
        assertEquals(Outcome.Failure(AppError.Network), dataSource.health())
    }
}
