package com.example.fundtracker.data.room

import android.content.Context
import androidx.room.Room
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fund_tracker_db"
        ).build()
    }

    // THIS IS WHAT IS MISSING:
    @Provides
    fun providePortfolioDao(database: AppDatabase): PortfolioDao {
        return database.portfolioDao()
    }
}