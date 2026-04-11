package com.example.fundtracker.ui.features.navigation

import kotlinx.serialization.Serializable

@Serializable sealed interface NavRoutes {

    @Serializable
    object Explore : NavRoutes

    @Serializable
    object Search : NavRoutes

    @Serializable
    data class FundList(val title: String, val category: String)

    @Serializable
    data class ProductDetails(val schemeCode: Int)
}