package com.example.fundtracker.data.model

data class FundMarketData(
    val currentNav: String,
    val dayChangePercent: Double,
    val isPositive: Boolean
)