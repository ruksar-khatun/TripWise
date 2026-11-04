package com.tripwise.core.network.datasource

import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.error.AiFailure
import com.tripwise.core.network.NetworkConfig
import com.tripwise.core.network.di.Authenticated
import com.tripwise.core.network.error.ApiErrorMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Serializable data class ChatMessageDto(val role: String, val text: String)
@Serializable data class ChatTripDto(val title: String, val destinationName: String, val startDate: String, val endDate: String, val travellers: Int)
@Serializable data class ChatContextDto(val destinationId: String? = null, val trip: ChatTripDto? = null)
@Serializable data class ChatRequestDto(val messages: List<ChatMessageDto>, val context: ChatContextDto? = null)

sealed interface ChatStreamEvent {
    data class Delta(val text: String) : ChatStreamEvent
    data object Done : ChatStreamEvent
    data class Failed(val error: AppError) : ChatStreamEvent
}

/** Reads the backend's Server-Sent Events chat stream over the authenticated client. */
@Singleton
class AssistantNetworkDataSource @Inject constructor(
    @Authenticated client: OkHttpClient,
    private val config: NetworkConfig,
    private val json: Json,
    private val errors: ApiErrorMapper,
) {
    // Replies can pause between chunks while the model thinks.
    private val streamingClient = client.newBuilder().readTimeout(90, TimeUnit.SECONDS).build()

    fun chat(request: ChatRequestDto): Flow<ChatStreamEvent> = flow {
        val http = Request.Builder()
            .url(config.baseUrl.trimEnd('/') + "/api/v1/ai/chat")
            .header("Accept", "text/event-stream")
            .post(json.encodeToString(ChatRequestDto.serializer(), request).toRequestBody("application/json".toMediaType()))
            .build()
        try {
            streamingClient.newCall(http).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val error = errors.map(HttpException(Response.error<Any>(response.code, body.toResponseBody("application/json".toMediaType()))))
                    emit(ChatStreamEvent.Failed(error))
                    return@use
                }
                val source = response.body!!.source()
                var event: String? = null
                var finished = false
                while (!finished) {
                    val line = source.readUtf8Line() ?: break
                    when {
                        line.startsWith("event:") -> event = line.removePrefix("event:").trim()
                        line.startsWith("data:") -> {
                            val obj = runCatching { json.decodeFromString(JsonObject.serializer(), line.removePrefix("data:").trim()) }.getOrNull()
                            if (event == "error") {
                                val code = obj?.get("code")?.jsonPrimitive?.contentOrNull
                                emit(ChatStreamEvent.Failed(if (code == "AI_INVALID_OUTPUT") AppError.Ai(AiFailure.INVALID_OUTPUT) else AppError.Ai(AiFailure.UNAVAILABLE)))
                                finished = true
                            } else if (obj?.get("done")?.jsonPrimitive?.booleanOrNull == true) {
                                emit(ChatStreamEvent.Done)
                                finished = true
                            } else {
                                obj?.get("delta")?.jsonPrimitive?.contentOrNull?.let { emit(ChatStreamEvent.Delta(it)) }
                            }
                        }
                        line.isEmpty() -> event = null
                    }
                }
                if (!finished) emit(ChatStreamEvent.Failed(AppError.Network)) // stream cut off
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ChatStreamEvent.Failed(errors.map(e)))
        }
    }.flowOn(Dispatchers.IO)
}
