package com.example.fundtracker.data.model

import com.google.gson.annotations.SerializedName

data class FundMeta(
    @SerializedName("fund_house") val fundHouse: String,
    @SerializedName("scheme_type") val schemeType: String,
    @SerializedName("scheme_category") val schemeCategory: String,
    @SerializedName("scheme_name") val schemeName: String,
    @SerializedName("scheme_code") val schemeCode: Int
)
