package com.example.fundtracker.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fundtracker.data.model.ExploreCacheEntity
import com.example.fundtracker.data.model.ExploreState
import com.example.fundtracker.data.model.FundMarketData
import com.example.fundtracker.data.model.FundSearchResult
import com.example.fundtracker.data.remote.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: FundRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Observe DB directly. UI updates automatically when DB changes.
    val indexFunds = repository.getCachedExploreFunds("INDEX").mapToState()
    val bluechipFunds = repository.getCachedExploreFunds("BLUECHIP").mapToState()
    val taxSaverFunds = repository.getCachedExploreFunds("TAX").mapToState()
    val largeCapFunds = repository.getCachedExploreFunds("LARGE_CAP").mapToState()

    init {
        refreshAllData()
    }

    private fun refreshAllData() {
        viewModelScope.launch {
            _isLoading.value = true
            // Run all refreshes in parallel. If offline, these fail silently and DB stays intact.
            val jobs = listOf(
                launch { repository.refreshExploreCache("INDEX", "index") },
                launch { repository.refreshExploreCache("BLUECHIP", "bluechip") },
                launch { repository.refreshExploreCache("TAX", "tax") },
                launch { repository.refreshExploreCache("LARGE_CAP", "large cap") }
            )
            jobs.joinAll()
            _isLoading.value = false
        }
    }

    // Helper to convert DB Flow to UI List
    private fun Flow<List<ExploreCacheEntity>>.mapToState() = this.map { list ->
        list.map { entity ->
            FundSearchResult(entity.schemeCode, entity.schemeName) to FundMarketData(
                currentNav = entity.currentNav ?: "---",
                dayChangePercent = entity.dayChangePercent ?: 0.0,
                isPositive = entity.isPositive ?: true
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}