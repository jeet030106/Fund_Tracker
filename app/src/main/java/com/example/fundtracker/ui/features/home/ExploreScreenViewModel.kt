package com.example.fundtracker.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fundtracker.data.model.ExploreState
import com.example.fundtracker.data.model.FundMarketData
import com.example.fundtracker.data.model.FundSearchResult
import com.example.fundtracker.data.remote.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: FundRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreState())
    val state = _state.asStateFlow()

    init {
        fetchExploreData()
    }

    private fun fetchExploreData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch search results first
                val indexSearch = async { repository.searchFunds("index").take(4) }
                val bluechipSearch = async { repository.searchFunds("bluechip").take(4) }
                val taxSearch = async { repository.searchFunds("tax").take(4) }
                val largeCapSearch = async { repository.searchFunds("large cap").take(4) }

                val indexWithData = fetchMarketDataForList(indexSearch.await())
                val bluechipWithData = fetchMarketDataForList(bluechipSearch.await())
                val taxWithData = fetchMarketDataForList(taxSearch.await())
                val largeCapWithData = fetchMarketDataForList(largeCapSearch.await())

                _state.update { it.copy(
                    isLoading = false,
                    indexFunds = indexWithData,
                    bluechipFunds = bluechipWithData,
                    taxSaverFunds = taxWithData,
                    largeCapFunds = largeCapWithData
                )}
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // Helper to fetch details for a list of search results in parallel
    private suspend fun fetchMarketDataForList(list: List<FundSearchResult>): List<Pair<FundSearchResult, FundMarketData?>> {
        return list.map { fund ->
            viewModelScope.async {
                val data = repository.getFundMarketData(fund.schemeCode)
                fund to data
            }
        }.awaitAll()
    }
}