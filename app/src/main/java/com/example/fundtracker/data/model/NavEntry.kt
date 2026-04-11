package com.example.fundtracker.data.model

import com.google.gson.annotations.SerializedName

data class NavEntry(
    @SerializedName("date") val date: String,
    @SerializedName("nav") val nav: String
)
