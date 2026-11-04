package com.tripwise.core.common.time

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

/** Inject [Clock] instead of calling `LocalDate.now()` so time-dependent logic is testable. */
@Module
@InstallIn(SingletonComponent::class)
object TimeModule {
    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()
}
