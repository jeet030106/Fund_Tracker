package com.example.fundtracker.ui.features.global_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fundtracker.data.model.FundSearchResult
import com.example.fundtracker.data.remote.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlobalFundListViewModel @Inject constructor(
    private val repository: FundRepository
) : ViewModel() {

    private val _rawFunds = MutableStateFlow<List<FundSearchResult>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All Funds")
    val selectedCategory = _selectedCategory.asStateFlow()

    val categories = listOf("All Funds", "Growth", "Liquid", "Direct", "Income", "Dividend", "Pension")

    // Simple StateFlow of the filtered list
    val filteredFunds: StateFlow<List<FundSearchResult>> = combine(
        _rawFunds,
        _selectedCategory
    ) { funds, category ->
        if (category == "All Funds") {
            funds
        } else {
            funds.filter { it.schemeName.contains(category, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        fetchAllFunds()
    }

    private fun fetchAllFunds() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch the list (Make sure your Repo doesn't require page/limit anymore,
                // or pass large numbers to get "everything")
                val result = repository.getAllAvailableFunds(page = 1, limit = 1000)
                _rawFunds.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }
}