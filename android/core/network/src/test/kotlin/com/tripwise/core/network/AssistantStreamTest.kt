package com.tripwise.core.network

import com.tripwise.core.common.error.AiFailure
import com.tripwise.core.common.error.AppError
import com.tripwise.core.network.datasource.AssistantNetworkDataSource
import com.tripwise.core.network.datasource.ChatMessageDto
import com.tripwise.core.network.datasource.ChatRequestDto
import com.tripwise.core.network.datasource.ChatStreamEvent
import com.tripwise.core.network.di.NetworkModule
import com.tripwise.core.network.di.buildOkHttpClient
import com.tripwise.core.network.error.ApiErrorMapper
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AssistantStreamTest {
    private val server = MockWebServer()
    private lateinit var source: AssistantNetworkDataSource
    private val request = ChatRequestDto(listOf(ChatMessageDto("user", "hi")))

    @Before fun setUp() {
        server.start()
        val json = NetworkModule.provideJson()
        val config = NetworkConfig(server.url("/").toString(), false, "test")
        source = AssistantNetworkDataSource(buildOkHttpClient(config), config, json, ApiErrorMapper(json))
    }

    @After fun tearDown() = server.close()

    private fun sse(body: String, code: Int = 200) = server.enqueue(
        MockResponse.Builder().code(code).addHeader("Content-Type", if (code == 200) "text/event-stream" else "application/json").body(body).build(),
    )

    @Test fun `parses deltas then done, and posts the request`() = runBlocking {
        sse("data: {\"delta\":\"Hel\"}\n\ndata: {\"delta\":\"lo\"}\n\ndata: {\"done\":true}\n\n")
        assertEquals(
            listOf(ChatStreamEvent.Delta("Hel"), ChatStreamEvent.Delta("lo"), ChatStreamEvent.Done),
            source.chat(request).toList(),
        )
        val recorded = server.takeRequest()
        assertEquals("/api/v1/ai/chat", recorded.url.encodedPath)
        assert(recorded.body!!.utf8().contains("\"text\":\"hi\""))
    }

    @Test fun `error event after partial output`() = runBlocking {
        sse("data: {\"delta\":\"Hel\"}\n\nevent: error\ndata: {\"code\":\"AI_UNAVAILABLE\",\"message\":\"x\"}\n\n")
        assertEquals(
            listOf(ChatStreamEvent.Delta("Hel"), ChatStreamEvent.Failed(AppError.Ai(AiFailure.UNAVAILABLE))),
            source.chat(request).toList(),
        )
    }

    @Test fun `http errors use the API error envelope`() = runBlocking {
        sse("{\"error\":{\"code\":\"NOT_CONFIGURED\",\"message\":\"no key\"}}", code = 503)
        assertEquals(listOf(ChatStreamEvent.Failed(AppError.NotConfigured)), source.chat(request).toList())
        sse("{\"error\":{\"code\":\"AI_QUOTA_EXCEEDED\",\"message\":\"limit\"}}", code = 429)
        assertEquals(listOf(ChatStreamEvent.Failed(AppError.Ai(AiFailure.QUOTA_EXCEEDED))), source.chat(request).toList())
    }

    @Test fun `stream cut off before done is a network failure`() = runBlocking {
        sse("data: {\"delta\":\"Hel\"}\n\n")
        assertEquals(listOf(ChatStreamEvent.Delta("Hel"), ChatStreamEvent.Failed(AppError.Network)), source.chat(request).toList())
    }
}
