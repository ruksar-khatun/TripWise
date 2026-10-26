package com.example.tripwise.data.chat

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

interface ChatApiService {
    @POST
    suspend fun sendMessage(
        @Url url: String,
        @Body request: ChatApiRequest
    ): Response<ChatApiResponse>

    companion object {
        fun create(): ChatApiService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl("http://localhost/") // Base URL placeholder when using @Url
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ChatApiService::class.java)
        }
    }
}
