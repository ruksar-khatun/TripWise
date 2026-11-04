package com.tripwise.core.data.di

import com.tripwise.core.common.network.NetworkMonitor
import com.tripwise.core.data.repository.DefaultSystemRepository
import com.tripwise.core.data.util.ConnectivityManagerNetworkMonitor
import com.tripwise.core.domain.repository.SystemRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds @Singleton
    abstract fun bindSystemRepository(impl: DefaultSystemRepository): SystemRepository

    @Binds @Singleton
    abstract fun bindNetworkMonitor(impl: ConnectivityManagerNetworkMonitor): NetworkMonitor
}
