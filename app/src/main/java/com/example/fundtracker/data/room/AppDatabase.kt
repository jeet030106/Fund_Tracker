package com.example.fundtracker.data.room


import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fundtracker.data.model.ExploreCacheEntity

@Database(
    entities = [
        PortfolioEntity::class,
        FundEntity::class,
        PortfolioFundCrossRef::class,
        ExploreCacheEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao
}