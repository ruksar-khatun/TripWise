package com.tripwise.app.di

import com.tripwise.app.BuildConfig
import com.tripwise.core.network.NetworkConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** App-level bindings: values that come from the app's build configuration. */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideNetworkConfig(): NetworkConfig = NetworkConfig(
        baseUrl = BuildConfig.BACKEND_BASE_URL,
        enableHttpLogging = BuildConfig.DEBUG,
        userAgent = "TripWise-Android/${BuildConfig.VERSION_NAME} (+https://github.com/ruksar-khatun/TripWise-v2)",
    )
}
