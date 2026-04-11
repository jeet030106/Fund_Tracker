package com.example.fundtracker.data.model

import com.google.gson.annotations.SerializedName

data class FundDetailsResponse(
    @SerializedName("meta") val meta: FundMeta,
    @SerializedName("data") val data: List<NavData>,
    @SerializedName("status") val status: String?
)
