package com.example.fundtracker.data.model

data class ExploreState(
    val indexFunds: List<Pair<FundSearchResult, FundMarketData?>> = emptyList(),
    val bluechipFunds: List<Pair<FundSearchResult, FundMarketData?>> = emptyList(),
    val taxSaverFunds: List<Pair<FundSearchResult, FundMarketData?>> = emptyList(),
    val largeCapFunds: List<Pair<FundSearchResult, FundMarketData?>> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
