package com.tripwise.core.data.di

import com.tripwise.core.common.network.NetworkMonitor
import com.tripwise.core.data.repository.CatalogDestinationRepository
import com.tripwise.core.data.repository.DefaultAuthRepository
import com.tripwise.core.data.repository.DefaultSystemRepository
import com.tripwise.core.data.repository.LocalUserData
import com.tripwise.core.data.repository.OfflineFirstSavedDestinationRepository
import com.tripwise.core.data.repository.OfflineFirstTripRepository
import com.tripwise.core.data.repository.RoomLocalUserData
import com.tripwise.core.data.repository.SessionUserRepository
import com.tripwise.core.data.util.ConnectivityManagerNetworkMonitor
import com.tripwise.core.domain.repository.AuthRepository
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
    @Binds @Singleton abstract fun bindSystemRepository(impl: DefaultSystemRepository): SystemRepository
    @Binds @Singleton abstract fun bindNetworkMonitor(impl: ConnectivityManagerNetworkMonitor): NetworkMonitor

    @Binds abstract fun bindAuthRepository(impl: DefaultAuthRepository): AuthRepository
    @Binds abstract fun bindUserRepository(impl: SessionUserRepository): UserRepository
    @Binds abstract fun bindLocalUserData(impl: RoomLocalUserData): LocalUserData

    @Binds abstract fun bindTripRepository(impl: OfflineFirstTripRepository): TripRepository
    @Binds abstract fun bindSavedDestinationRepository(impl: OfflineFirstSavedDestinationRepository): SavedDestinationRepository

    @Binds abstract fun bindPlacesRepository(impl: com.tripwise.core.data.repository.DefaultPlacesRepository): com.tripwise.core.domain.repository.PlacesRepository

    @Binds @Singleton abstract fun bindDestinationRepository(impl: CatalogDestinationRepository): DestinationRepository
}
