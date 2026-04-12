package com.example.fundtracker.data.remote

import android.util.Log
import com.example.fundtracker.data.model.ExploreCacheEntity
import com.example.fundtracker.data.model.FundDetailsResponse
import com.example.fundtracker.data.model.FundMarketData
import com.example.fundtracker.data.model.FundSearchResult
import com.example.fundtracker.data.room.FundEntity
import com.example.fundtracker.data.room.PortfolioDao
import com.example.fundtracker.data.room.PortfolioEntity
import com.example.fundtracker.data.room.PortfolioFundCrossRef
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FundRepository @Inject constructor(
    private val apiService: ApiService,
    private val portfolioDao: PortfolioDao
) {
    // --- API Methods ---

    suspend fun searchFunds(query: String): List<FundSearchResult> {
        return try {
            apiService.searchFunds(query)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFullFundDetails(schemeCode: Int): FundDetailsResponse {
        return apiService.getFundDetails(schemeCode)
    }

    suspend fun getExploreData(): Map<String, List<FundSearchResult>> {
        return mapOf(
            "Index Funds" to searchFunds("index").take(4),
            "Bluechip" to searchFunds("bluechip").take(4),
            "Tax Saver" to searchFunds("tax").take(4)
        )
    }

    suspend fun getFundMarketData(schemeCode: Int): FundMarketData? {
        return try {
            val response = apiService.getFundDetails(schemeCode)
            val history = response.data
            if (history.size >= 2) {
                val today = history[0].nav.toDouble()
                val yesterday = history[1].nav.toDouble()
                val percent = ((today - yesterday) / yesterday) * 100
                FundMarketData(
                    currentNav = history[0].nav,
                    dayChangePercent = percent,
                    isPositive = percent >= 0
                )
            } else null
        } catch (e: Exception) { null }
    }

    suspend fun getAllAvailableFunds(page : Int, limit : Int): List<FundSearchResult> {
        return apiService.getAllFunds(page, limit)
    }

    // --- Room / Portfolio Methods ---

    fun getAllPortfolios(): Flow<List<PortfolioEntity>> = portfolioDao.getAllPortfolios()

    fun getPortfolioWithFunds(id: Long) = portfolioDao.getPortfolioWithFunds(id)


    fun getCachedExploreFunds(category: String) = portfolioDao.getExploreCacheByCategory(category)

    suspend fun refreshExploreCache(category: String, query: String) {
        try {
            // 1. Fetch from Network
            val searchResults = apiService.searchFunds(query).take(4)

            // 2. Map to Entity (including market data)
            val entities = searchResults.map { fund ->
                val marketData = getFundMarketData(fund.schemeCode)
                ExploreCacheEntity(
                    schemeCode = fund.schemeCode,
                    schemeName = fund.schemeName,
                    categoryType = category,
                    currentNav = marketData?.currentNav,
                    dayChangePercent = marketData?.dayChangePercent,
                    isPositive = marketData?.isPositive
                )
            }

            // 3. Update Database (UI will react via Flow)
            portfolioDao.insertExploreCache(entities)
        } catch (e: Exception) {
            Log.e("ExploreRefresh", "Offline or API Error for $category: ${e.message}")
            // We don't throw here; the UI will simply keep showing the cached data
        }
    }

    /**
     * NEW: Fetches all portfolios that contain a specific fund.
     * Essential for the "multiple portfolio" check in ProductDetails.
     */
    fun getPortfoliosForFund(schemeCode: Int): Flow<List<PortfolioEntity>> {
        return portfolioDao.getPortfoliosForFund(schemeCode)
    }

    suspend fun insertPortfolio(portfolio: PortfolioEntity): Long {
        return portfolioDao.insertPortfolio(portfolio)
    }

    suspend fun syncPortfolioFunds(funds: List<FundEntity>) {
        funds.forEach { fund ->
            try {
                val response = apiService.getFundDetails(fund.schemeCode)
                val latestNav = response.data.firstOrNull()?.nav ?: fund.lastNav
                portfolioDao.insertFund(fund.copy(lastNav = latestNav))
            } catch (e: Exception) {
                Log.e("SYNC_ERROR", "Failed to sync ${fund.schemeName}")
            }
        }
    }

    suspend fun saveFundToPortfolio(portfolioId: Long, fund: FundEntity) {
        portfolioDao.insertFund(fund)
        portfolioDao.addFundToPortfolio(PortfolioFundCrossRef(portfolioId, fund.schemeCode))
    }

    /**
     * NEW: Removes a fund from a specific portfolio.
     * Deletes the entry from the CrossRef (Join) table.
     */
    suspend fun removeFundFromPortfolio(portfolioId: Long, schemeCode: Int) {
        portfolioDao.deleteFundFromPortfolio(PortfolioFundCrossRef(portfolioId, schemeCode))
    }
}