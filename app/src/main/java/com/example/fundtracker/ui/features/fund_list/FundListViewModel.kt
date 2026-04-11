package com.example.fundtracker.ui.features.fund_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
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

    private val _uiState = MutableStateFlow<Resource<List<FundSearchResult>>>(Resource.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        fetchFunds()
    }

    private fun fetchFunds() {
        viewModelScope.launch {
            _uiState.value = Resource.Loading
            try {
                val results = repository.searchFunds(category)
                _uiState.value = Resource.Success(results)
            } catch (e: Exception) {
                _uiState.value = Resource.Error(e.message ?: "Failed to load funds")
            }
        }
    }
}