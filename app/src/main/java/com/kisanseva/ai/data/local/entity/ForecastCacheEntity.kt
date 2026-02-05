package com.kisanseva.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kisanseva.ai.domain.model.ForecastResponse

@Entity(tableName = "forecast_cache")
data class ForecastCacheEntity(
    @PrimaryKey
    val id: String, // "lat,lon"
    val response: ForecastResponse,
    val timestamp: Long
)
