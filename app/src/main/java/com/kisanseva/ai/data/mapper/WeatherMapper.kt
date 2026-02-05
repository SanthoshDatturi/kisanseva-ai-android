package com.kisanseva.ai.data.mapper

import com.kisanseva.ai.data.local.entity.CurrentWeatherCacheEntity
import com.kisanseva.ai.data.local.entity.ForecastCacheEntity
import com.kisanseva.ai.data.local.entity.ReverseGeocodingCacheEntity
import com.kisanseva.ai.domain.model.CurrentWeatherResponse
import com.kisanseva.ai.domain.model.ForecastResponse
import com.kisanseva.ai.domain.model.GeocodingResponse

fun toCurrentWeatherCacheEntity(
    response: CurrentWeatherResponse,
    lat: Double,
    lon: Double
): CurrentWeatherCacheEntity {
    val cacheId = "$lat,$lon"
    return CurrentWeatherCacheEntity(
        id = cacheId,
        response = response,
        timestamp = System.currentTimeMillis()
    )
}

fun fromCurrentWeatherCacheEntity(
    entity: CurrentWeatherCacheEntity
): CurrentWeatherResponse {
    return entity.response
}

fun toForecastCacheEntity(
    response: ForecastResponse,
    lat: Double,
    lon: Double
): ForecastCacheEntity {
    val cacheId = "$lat,$lon"
    return ForecastCacheEntity(
        id = cacheId,
        response = response,
        timestamp = System.currentTimeMillis()
    )
}

fun fromForecastCacheEntity(
    entity: ForecastCacheEntity
): ForecastResponse {
    return entity.response
}

fun toReverseGeocodingCacheEntity(
    response: List<GeocodingResponse>,
    lat: Double,
    lon: Double
): ReverseGeocodingCacheEntity {
    val cacheId = "$lat,$lon"
    return ReverseGeocodingCacheEntity(
        id = cacheId,
        response = response,
        timestamp = System.currentTimeMillis()
    )
}

fun fromReverseGeocodingCacheEntity(
    entity: ReverseGeocodingCacheEntity
): List<GeocodingResponse> {
    return entity.response
}
