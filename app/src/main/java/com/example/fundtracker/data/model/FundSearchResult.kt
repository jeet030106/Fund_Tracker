package com.example.fundtracker.data.model

import com.google.gson.annotations.SerializedName

data class FundSearchResult(
    @SerializedName("schemeCode") val schemeCode: Int,
    @SerializedName("schemeName") val schemeName: String
)
