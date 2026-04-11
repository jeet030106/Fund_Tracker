package com.example.fundtracker.ui.features.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fundtracker.data.model.FundSearchResult
import com.example.fundtracker.data.remote.FundRepository
import com.example.fundtracker.ui.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: FundRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<Resource<List<FundSearchResult>>>(Resource.Success(emptyList()))
    val searchResults = _searchResults.asStateFlow()

    init {
        // Observe the search query and trigger API call after 500ms of inactivity
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .filter { it.trim().length >= 3 } // Only search if 3+ characters
                .distinctUntilChanged()
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    fun onQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        if (newQuery.isEmpty()) {
            _searchResults.value = Resource.Success(emptyList())
        }
    }

    private suspend fun performSearch(query: String) {
        _searchResults.value = Resource.Loading
        try {
            val results = repository.searchFunds(query)
            _searchResults.value = Resource.Success(results)
        } catch (e: Exception) {
            _searchResults.value = Resource.Error(e.message ?: "An error occurred")
        }
    }
}