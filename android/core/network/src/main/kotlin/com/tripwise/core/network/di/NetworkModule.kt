package com.tripwise.core.network.di

import com.tripwise.core.network.NetworkConfig
import com.tripwise.core.network.api.TripWiseApi
import com.tripwise.core.network.datasource.RetrofitTripWiseNetworkDataSource
import com.tripwise.core.network.datasource.TripWiseNetworkDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(config: NetworkConfig): OkHttpClient = buildOkHttpClient(config)

    @Provides
    @Singleton
    fun provideRetrofit(config: NetworkConfig, json: Json, client: OkHttpClient): Retrofit =
        buildRetrofit(config.baseUrl, json, client)

    @Provides
    @Singleton
    fun provideTripWiseApi(retrofit: Retrofit): TripWiseApi = retrofit.create(TripWiseApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class NetworkBindingsModule {
    @Binds
    abstract fun bindNetworkDataSource(impl: RetrofitTripWiseNetworkDataSource): TripWiseNetworkDataSource
}

internal fun buildOkHttpClient(config: NetworkConfig): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(userAgentInterceptor(config.userAgent))
    // Auth (ID token) and App Check interceptors are added with the authentication phase.
    .apply {
        if (config.enableHttpLogging) {
            addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        }
    }
    .build()

internal fun buildRetrofit(baseUrl: String, json: Json, client: OkHttpClient): Retrofit = Retrofit.Builder()
    .baseUrl(baseUrl)
    .client(client)
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .build()

private fun userAgentInterceptor(userAgent: String) = Interceptor { chain ->
    chain.proceed(chain.request().newBuilder().header("User-Agent", userAgent).build())
}
