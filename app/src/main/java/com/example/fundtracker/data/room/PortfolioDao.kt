package com.example.fundtracker.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.fundtracker.data.model.ExploreCacheEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface PortfolioDao {
    // --- PORTFOLIO QUERIES ---

    @Query("SELECT * FROM portfolios")
    fun getAllPortfolios(): Flow<List<PortfolioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolio(portfolio: PortfolioEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFund(fund: FundEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFundToPortfolio(crossRef: PortfolioFundCrossRef)

    @Transaction
    @Query("SELECT * FROM portfolios WHERE id = :portfolioId")
    fun getPortfolioWithFunds(portfolioId: Long): Flow<PortfolioWithFunds>

    /**
     * Finds all portfolios that contain a specific fund.
     * Essential for the heart icon toggle logic.
     */
    @Query("""
        SELECT * FROM portfolios 
        INNER JOIN portfolio_fund_cross_ref ON portfolios.id = portfolio_fund_cross_ref.portfolioId 
        WHERE portfolio_fund_cross_ref.schemeCode = :schemeCode
    """)
    fun getPortfoliosForFund(schemeCode: Int): Flow<List<PortfolioEntity>>

    @Delete
    suspend fun deleteFundFromPortfolio(crossRef: PortfolioFundCrossRef)


    // --- EXPLORE PAGE CACHE QUERIES ---

    @Query("SELECT * FROM explore_cache WHERE categoryType = :category")
    fun getExploreCacheByCategory(category: String): Flow<List<ExploreCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExploreCache(funds: List<ExploreCacheEntity>)

    @Query("DELETE FROM explore_cache WHERE categoryType = :category")
    suspend fun clearExploreCacheByCategory(category: String)
}