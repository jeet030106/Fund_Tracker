package com.example.fundtracker.data.remote

import android.util.Log
import com.example.fundtracker.data.model.FundMarketData
import com.example.fundtracker.data.model.FundSearchResult
import javax.inject.Inject
import kotlin.collections.get
import kotlin.compareTo

class FundRepository @Inject constructor(
    private val apiService: ApiService
) {
    // For Search Screen
    suspend fun searchFunds(query: String) = apiService.searchFunds(query)

    // For Product Screen
    suspend fun getDetails(id: Int) = apiService.getFundDetails(id)

    // For Explore Screen (Requirement: Fetch specific categories)
    suspend fun getExploreData(): Map<String, List<FundSearchResult>> {
        return mapOf(
            "Index Funds" to apiService.searchFunds("index").take(4),
            "Bluechip" to apiService.searchFunds("bluechip").take(4),
            "Tax Saver" to apiService.searchFunds("tax").take(4)
        )
    }
    suspend fun testApiFetch() {
        try {
            val response = apiService.searchFunds("HDFC")
            Log.d("API_TEST", "Successfully fetched ${response.size} funds")
        } catch (e: Exception) {
            Log.e("API_TEST", "Error fetching data: ${e.message}")
        }
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
}