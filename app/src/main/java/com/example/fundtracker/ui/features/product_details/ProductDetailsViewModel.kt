package com.example.fundtracker.ui.features.product_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fundtracker.data.model.FundDetailsResponse
import com.example.fundtracker.data.remote.FundRepository
import com.example.fundtracker.ui.features.navigation.NavRoutes
import com.example.fundtracker.ui.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val repository: FundRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Retrieve schemeCode from NavRoute
    private val schemeCode: Int = savedStateHandle.toRoute<NavRoutes.ProductDetails>().schemeCode

    private val _uiState = MutableStateFlow<Resource<FundDetailsResponse>>(Resource.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        fetchProductDetails()
    }

    private fun fetchProductDetails() {
        viewModelScope.launch {
            _uiState.value = Resource.Loading
            try {
                val response = repository.getFullFundDetails(schemeCode)
                _uiState.value = Resource.Success(response)
            } catch (e: Exception) {
                _uiState.value = Resource.Error(e.message ?: "Failed to load fund details")
            }
        }
    }
}