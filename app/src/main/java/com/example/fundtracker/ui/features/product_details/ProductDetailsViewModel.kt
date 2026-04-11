package com.example.fundtracker.ui.features.product_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fundtracker.data.model.FundDetailsResponse
import com.example.fundtracker.data.remote.FundRepository
import com.example.fundtracker.data.room.FundEntity
import com.example.fundtracker.data.room.PortfolioEntity
import com.example.fundtracker.ui.features.navigation.NavRoutes
import com.example.fundtracker.ui.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val repository: FundRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val schemeCode: Int = savedStateHandle.toRoute<NavRoutes.ProductDetails>().schemeCode

    private val _uiState = MutableStateFlow<Resource<FundDetailsResponse>>(Resource.Loading)
    val uiState = _uiState.asStateFlow()

    // Observe portfolios from Room for the Dialog
    val portfolios: StateFlow<List<PortfolioEntity>> = repository.getAllPortfolios()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    // Logic to save to an existing portfolio
    fun saveToPortfolio(portfolioId: Long, fund: FundDetailsResponse) {
        viewModelScope.launch {
            repository.saveFundToPortfolio(portfolioId, mapToEntity(fund))
        }
    }

    // Logic to create a new portfolio and then save the fund
    fun createAndSavePortfolio(name: String, fund: FundDetailsResponse) {
        viewModelScope.launch {
            val newId = repository.insertPortfolio(PortfolioEntity(name = name))
            repository.saveFundToPortfolio(newId, mapToEntity(fund))
        }
    }

    private fun mapToEntity(fund: FundDetailsResponse): FundEntity {
        return FundEntity(
            schemeCode = fund.meta.schemeCode,
            schemeName = fund.meta.schemeName,
            fundHouse = fund.meta.fundHouse,
            category = fund.meta.schemeCategory,
            lastNav = fund.data.firstOrNull()?.nav ?: "0.0"
        )
    }
}