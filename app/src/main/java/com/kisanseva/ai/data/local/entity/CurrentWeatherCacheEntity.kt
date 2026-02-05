package com.kisanseva.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kisanseva.ai.domain.model.CurrentWeatherResponse

@Entity(tableName = "current_weather_cache")
data class CurrentWeatherCacheEntity(
    @PrimaryKey
    val id: String, // "lat,lon"
    val response: CurrentWeatherResponse,
    val timestamp: Long
)
