package com.tripwise.core.data.di

import com.tripwise.core.common.network.NetworkMonitor
import com.tripwise.core.data.demo.DemoDestinationRepository
import com.tripwise.core.data.demo.DemoSavedDestinationRepository
import com.tripwise.core.data.demo.DemoTripRepository
import com.tripwise.core.data.demo.DemoUserRepository
import com.tripwise.core.data.repository.DefaultSystemRepository
import com.tripwise.core.data.util.ConnectivityManagerNetworkMonitor
import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.domain.repository.SavedDestinationRepository
import com.tripwise.core.domain.repository.SystemRepository
import com.tripwise.core.domain.repository.TripRepository
import com.tripwise.core.domain.repository.UserRepository
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

    // ---- UI phase: demo data. Each binding is replaced by a real implementation in its feature phase. ----
    @Binds abstract fun bindDestinationRepository(impl: DemoDestinationRepository): DestinationRepository

    @Binds abstract fun bindSavedDestinationRepository(impl: DemoSavedDestinationRepository): SavedDestinationRepository

    @Binds abstract fun bindTripRepository(impl: DemoTripRepository): TripRepository

    @Binds abstract fun bindUserRepository(impl: DemoUserRepository): UserRepository
}
