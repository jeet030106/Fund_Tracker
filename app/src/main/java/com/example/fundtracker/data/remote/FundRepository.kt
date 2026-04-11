package com.example.fundtracker.data.remote

import android.util.Log
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

    // --- Room / Portfolio Methods ---

    fun getAllPortfolios() = portfolioDao.getAllPortfolios()

    fun getPortfolioWithFunds(id: Long) = portfolioDao.getPortfolioWithFunds(id)

    suspend fun insertPortfolio(portfolio: PortfolioEntity): Long {
        return portfolioDao.insertPortfolio(portfolio)
    }

    /**
     * Logic: Loops through funds in a portfolio, fetches fresh NAVs from API,
     * and updates the local Room database.
     */
    suspend fun syncPortfolioFunds(funds: List<FundEntity>) {
        funds.forEach { fund ->
            try {
                val response = apiService.getFundDetails(fund.schemeCode)
                val latestNav = response.data.firstOrNull()?.nav ?: fund.lastNav
                // Update the local DB with the fresh price
                portfolioDao.insertFund(fund.copy(lastNav = latestNav))
            } catch (e: Exception) {
                Log.e("SYNC_ERROR", "Failed to sync ${fund.schemeName}")
            }
        }
    }

    /**
     * Room: Save logic. Adds the fund info to the 'funds' table and
     * creates the link in the cross-reference table.
     */
    suspend fun saveFundToPortfolio(portfolioId: Long, fund: FundEntity) {
        portfolioDao.insertFund(fund)
        portfolioDao.addFundToPortfolio(PortfolioFundCrossRef(portfolioId, fund.schemeCode))
    }
}