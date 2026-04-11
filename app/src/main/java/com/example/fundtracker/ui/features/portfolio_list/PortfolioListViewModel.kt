package com.example.fundtracker.ui.features.portfolio_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fundtracker.data.remote.FundRepository
import com.example.fundtracker.data.room.PortfolioWithFunds
import com.example.fundtracker.data.room.FundEntity
import com.example.fundtracker.data.room.PortfolioEntity
import com.example.fundtracker.ui.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val repository: FundRepository
) : ViewModel() {

    // 1. Handled State for all portfolios (Main List)
    val allPortfolios: StateFlow<Resource<List<PortfolioEntity>>> = repository.getAllPortfolios()
        .map { Resource.Success(it) as Resource<List<PortfolioEntity>> }
        .onStart { emit(Resource.Loading) }
        .catch { emit(Resource.Error(it.message ?: "Failed to load portfolios")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    // 2. Handled State for selected portfolio (Details)
    private val _selectedPortfolio = MutableStateFlow<Resource<PortfolioWithFunds>>(Resource.Loading)
    val selectedPortfolio = _selectedPortfolio.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private var hasSyncedThisSession = false

    fun loadPortfolioDetails(id: Long) {
        viewModelScope.launch {
            repository.getPortfolioWithFunds(id)
                .onStart { _selectedPortfolio.value = Resource.Loading }
                .catch { e -> _selectedPortfolio.value = Resource.Error(e.message ?: "An error occurred") }
                .collect { data ->
                    if (data != null) {
                        _selectedPortfolio.value = Resource.Success(data)

                        // Sync logic remains unchanged, but we verify data presence
                        if (data.funds.isNotEmpty() && !hasSyncedThisSession) {
                            hasSyncedThisSession = true
                            syncPrices(data.funds)
                        }
                    } else {
                        _selectedPortfolio.value = Resource.Error("Portfolio not found")
                    }
                }
        }
    }

    private fun syncPrices(funds: List<FundEntity>) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.syncPortfolioFunds(funds)
            } catch (e: Exception) {
                // We don't change the whole screen to Error state here
                // because we want to keep showing the cached data
                // even if the background refresh fails.
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun resetSyncFlag() {
        hasSyncedThisSession = false
    }
}