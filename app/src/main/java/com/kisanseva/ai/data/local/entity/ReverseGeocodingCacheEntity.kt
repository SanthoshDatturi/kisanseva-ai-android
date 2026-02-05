package com.kisanseva.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kisanseva.ai.domain.model.GeocodingResponse

@Entity(tableName = "reverse_geocoding_cache")
data class ReverseGeocodingCacheEntity(
    @PrimaryKey
    val id: String, // "lat,lon"
    val response: List<GeocodingResponse>,
    val timestamp: Long
)
