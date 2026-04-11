package com.example.fundtracker.data.room


import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PortfolioEntity::class,
        FundEntity::class,
        PortfolioFundCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao
}