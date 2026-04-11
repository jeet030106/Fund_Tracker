package com.example.fundtracker.ui.features.portfolio_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fundtracker.data.remote.FundRepository
import com.example.fundtracker.data.room.PortfolioWithFunds
import com.example.fundtracker.data.room.FundEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val repository: FundRepository
) : ViewModel() {

    // 1. Get all portfolios for the main list screen
    val allPortfolios = repository.getAllPortfolios()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. State for a specific selected portfolio details
    private val _selectedPortfolio = MutableStateFlow<PortfolioWithFunds?>(null)
    val selectedPortfolio = _selectedPortfolio.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    // Flag to prevent the infinite sync loop
    private var hasSyncedThisSession = false

    fun loadPortfolioDetails(id: Long) {
        viewModelScope.launch {
            repository.getPortfolioWithFunds(id).collect { data ->
                _selectedPortfolio.value = data

                // Only trigger sync if we haven't synced yet and data is available
                if (data != null && data.funds.isNotEmpty() && !hasSyncedThisSession) {
                    hasSyncedThisSession = true // Mark as synced immediately
                    syncPrices(data.funds)
                }
            }
        }
    }

    /**
     * Updates the database with fresh NAV values from the API.
     * Since the UI observes Room, it will update automatically.
     */
    private fun syncPrices(funds: List<FundEntity>) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.syncPortfolioFunds(funds)
            } catch (e: Exception) {
                // Log error or handle failure
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Reset the flag when the user leaves the screen so it can sync again next time.
     */
    fun resetSyncFlag() {
        hasSyncedThisSession = false
    }
}