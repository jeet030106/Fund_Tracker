package com.example.fundtracker.data.remote

import android.util.Log
import com.example.fundtracker.data.model.FundSearchResult
import javax.inject.Inject

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
}