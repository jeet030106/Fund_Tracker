package com.example.fundtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "explore_cache")
data class ExploreCacheEntity(
    @PrimaryKey val schemeCode: Int,
    val schemeName: String,
    val categoryType: String, // "INDEX", "BLUECHIP", "TAX", "LARGE_CAP"
    val currentNav: String?,
    val dayChangePercent: Double?,
    val isPositive: Boolean?
)