package com.example.fundtracker.ui.features.global_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fundtracker.data.model.FundSearchResult
import com.example.fundtracker.data.remote.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlobalFundListViewModel @Inject constructor(
    private val repository: FundRepository
) : ViewModel() {

    private val _allFunds = MutableStateFlow<List<FundSearchResult>>(emptyList())
    private val _selectedFilter = MutableStateFlow("All")
    private val _isLoading = MutableStateFlow(false)

    // Combined state: Filters the list whenever the filter or data changes
    val filteredFunds = combine(_allFunds, _selectedFilter) { funds, filter ->
        when (filter) {
            "Growth" -> funds.filter { it.schemeName.contains("Growth", ignoreCase = true) }
            "Income" -> funds.filter { it.schemeName.contains("Income", ignoreCase = true) || it.schemeName.contains("IDCW", ignoreCase = true) }
            else -> funds
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedFilter = _selectedFilter.asStateFlow()
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchAllFunds()
    }

    private fun fetchAllFunds() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetching a broad set of funds (using an empty search or common prefix)
                val response = repository.searchFunds("Tata") // You can change this to any broad search
                _allFunds.value = response
            } catch (e: Exception) { /* handle error */ }
            _isLoading.value = false
        }
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }
}