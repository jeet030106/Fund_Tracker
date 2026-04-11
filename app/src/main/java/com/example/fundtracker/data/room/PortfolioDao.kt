package com.example.fundtracker.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolios")
    fun getAllPortfolios(): Flow<List<PortfolioEntity>>

    // Changed to return Long to get the ID of a newly created portfolio
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolio(portfolio: PortfolioEntity): Long

    // Required to cache fund details (name, category, lastNav) locally
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFund(fund: FundEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFundToPortfolio(crossRef: PortfolioFundCrossRef)

    @Transaction
    @Query("SELECT * FROM portfolios WHERE id = :portfolioId")
    fun getPortfolioWithFunds(portfolioId: Long): Flow<PortfolioWithFunds>
}