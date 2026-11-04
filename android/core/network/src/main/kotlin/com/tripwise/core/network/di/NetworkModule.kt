package com.tripwise.core.network.di

import com.tripwise.core.network.NetworkConfig
import com.tripwise.core.network.api.AuthApi
import com.tripwise.core.network.api.DestinationApi
import com.tripwise.core.network.datasource.CatalogNetworkDataSource
import com.tripwise.core.network.datasource.RetrofitCatalogNetworkDataSource
import com.tripwise.core.network.api.MeApi
import com.tripwise.core.network.api.TripWiseApi
import com.tripwise.core.network.auth.AuthInterceptor
import com.tripwise.core.network.auth.TokenAuthenticator
import com.tripwise.core.network.datasource.AuthNetworkDataSource
import com.tripwise.core.network.datasource.RetrofitAuthNetworkDataSource
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
import javax.inject.Qualifier
import javax.inject.Singleton

/** Client/Retrofit that attaches the user's access token and refreshes it on 401. */
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class Authenticated

/** Client/Retrofit without credentials (sign-in, refresh, health). */
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class Public

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

    @Provides @Singleton @Public
    fun providePublicClient(config: NetworkConfig): OkHttpClient = buildOkHttpClient(config)

    @Provides @Singleton @Authenticated
    fun provideAuthenticatedClient(
        @Public base: OkHttpClient,
        authInterceptor: AuthInterceptor,
        authenticator: TokenAuthenticator,
    ): OkHttpClient = base.newBuilder().addInterceptor(authInterceptor).authenticator(authenticator).build()

    @Provides @Singleton @Public
    fun providePublicRetrofit(config: NetworkConfig, json: Json, @Public client: OkHttpClient): Retrofit =
        buildRetrofit(config.baseUrl, json, client)

    @Provides @Singleton @Authenticated
    fun provideAuthenticatedRetrofit(config: NetworkConfig, json: Json, @Authenticated client: OkHttpClient): Retrofit =
        buildRetrofit(config.baseUrl, json, client)

    @Provides @Singleton
    fun provideTripWiseApi(@Public retrofit: Retrofit): TripWiseApi = retrofit.create(TripWiseApi::class.java)

    @Provides @Singleton
    fun provideAuthApi(@Public retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides @Singleton
    fun provideDestinationApi(@Public retrofit: Retrofit): DestinationApi = retrofit.create(DestinationApi::class.java)

    @Provides @Singleton
    fun providePlacesApi(@Authenticated retrofit: Retrofit): com.tripwise.core.network.api.PlacesApi =
        retrofit.create(com.tripwise.core.network.api.PlacesApi::class.java)

    @Provides @Singleton
    fun provideItineraryApi(@Authenticated retrofit: Retrofit): com.tripwise.core.network.api.ItineraryApi =
        retrofit.create(com.tripwise.core.network.api.ItineraryApi::class.java)

    @Provides @Singleton
    fun provideCurrencyApi(@Public retrofit: Retrofit): com.tripwise.core.network.api.CurrencyApi =
        retrofit.create(com.tripwise.core.network.api.CurrencyApi::class.java)

    @Provides @Singleton
    fun provideMeApi(@Authenticated retrofit: Retrofit): MeApi = retrofit.create(MeApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class NetworkBindingsModule {
    @Binds
    abstract fun bindNetworkDataSource(impl: RetrofitTripWiseNetworkDataSource): TripWiseNetworkDataSource

    @Binds
    abstract fun bindAuthNetworkDataSource(impl: RetrofitAuthNetworkDataSource): AuthNetworkDataSource

    @Binds
    abstract fun bindCatalogNetworkDataSource(impl: RetrofitCatalogNetworkDataSource): CatalogNetworkDataSource
}

internal fun buildOkHttpClient(config: NetworkConfig): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(userAgentInterceptor(config.userAgent))
    .apply {
        if (config.enableHttpLogging) {
            // BASIC never logs headers or bodies, so tokens and passwords stay out of logcat.
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
