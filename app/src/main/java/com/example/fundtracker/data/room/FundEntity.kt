package com.example.fundtracker.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "funds")
data class FundEntity(
    @PrimaryKey
    val schemeCode: Int,
    val schemeName: String,
    val fundHouse: String,
    val category: String,
    val lastNav: String
)