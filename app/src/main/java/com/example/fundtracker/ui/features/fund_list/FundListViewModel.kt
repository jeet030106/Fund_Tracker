package com.example.fundtracker.ui.features.fund_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fundtracker.data.model.FundMarketData
import com.example.fundtracker.data.model.FundSearchResult
import com.example.fundtracker.data.remote.FundRepository
import com.example.fundtracker.ui.features.navigation.NavRoutes
import com.example.fundtracker.ui.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class FundListViewModel @Inject constructor(
    private val repository: FundRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val category: String = savedStateHandle.toRoute<NavRoutes.FundList>().category

    private val _uiState = MutableStateFlow<Resource<List<Pair<FundSearchResult, FundMarketData?>>>>(Resource.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        fetchFunds()
    }

    private fun fetchFunds() {
        viewModelScope.launch {
            _uiState.value = Resource.Loading
            try {
                // 1. Get the list of funds (Names and Codes only)
                val searchResults = repository.searchFunds(category)

                // 2. Map to pairs with null market data and show UI immediately
                var currentList = searchResults.map { it to null as FundMarketData? }
                _uiState.value = Resource.Success(currentList)

                // 3. BACKGROUND SYNC: Fetch prices for the first 15 funds only
                // (Fetching all 100+ would be too slow/heavy)
                searchResults.take(15).forEachIndexed { index, fund ->
                    launch { // launch each in a separate coroutine for speed
                        try {
                            val details = repository.getFullFundDetails(fund.schemeCode)
                            val price = details.data.firstOrNull()?.nav ?: "0.0"

                            // Update the specific item in the list
                            val marketData = FundMarketData(
                                currentNav = price,
                                dayChangePercent = 0.0, // Calculated if needed
                                isPositive = true
                            )

                            // Create a new list with the updated item to trigger UI refresh
                            currentList = currentList.toMutableList().apply {
                                this[index] = fund to marketData
                            }
                            _uiState.value = Resource.Success(currentList)
                        } catch (e: Exception) {
                            // Skip if a single price fetch fails
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = Resource.Error(e.message ?: "Failed to load funds")
            }
        }
    }
}