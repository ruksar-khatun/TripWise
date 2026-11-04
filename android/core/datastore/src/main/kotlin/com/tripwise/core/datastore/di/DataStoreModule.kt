package com.tripwise.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.tripwise.core.common.coroutines.Dispatcher
import com.tripwise.core.common.coroutines.TripWiseDispatcher
import com.tripwise.core.datastore.EncryptedTokenStore
import com.tripwise.core.network.auth.TokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier @Retention(AnnotationRetention.BINARY) internal annotation class TokenData

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /** App preferences and cached (non-secret) profile. File: tripwise_prefs.preferences_pb */
    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
        @Dispatcher(TripWiseDispatcher.IO) ioDispatcher: CoroutineDispatcher,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        scope = CoroutineScope(ioDispatcher + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile("tripwise_prefs") },
    )

    /** Encrypted session tokens only. File: tripwise_session.preferences_pb (excluded from backup). */
    @Provides
    @Singleton
    @TokenData
    internal fun provideTokenDataStore(
        @ApplicationContext context: Context,
        @Dispatcher(TripWiseDispatcher.IO) ioDispatcher: CoroutineDispatcher,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        scope = CoroutineScope(ioDispatcher + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile("tripwise_session") },
    )

    @Provides
    @Singleton
    internal fun provideTokenStore(
        @ApplicationContext context: Context,
        @TokenData dataStore: DataStore<Preferences>,
    ): TokenStore = EncryptedTokenStore(context, dataStore)
}
