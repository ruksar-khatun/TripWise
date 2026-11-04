package com.tripwise.core.database.di

import android.content.Context
import androidx.room.Room
import com.tripwise.core.database.TripWiseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TripWiseDatabase =
        Room.databaseBuilder(context, TripWiseDatabase::class.java, "tripwise.db").build()

    @Provides fun provideTripDao(db: TripWiseDatabase) = db.tripDao()
    @Provides fun provideItineraryDao(db: TripWiseDatabase) = db.itineraryDao()
    @Provides fun provideBudgetDao(db: TripWiseDatabase) = db.budgetDao()
    @Provides fun provideSavedDestinationDao(db: TripWiseDatabase) = db.savedDestinationDao()
    @Provides fun provideExpenseDao(db: TripWiseDatabase) = db.expenseDao()
}
