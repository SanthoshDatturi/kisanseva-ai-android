package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.local.dao.CurrentWeatherCacheDao
import com.kisanseva.ai.data.local.dao.ForecastCacheDao
import com.kisanseva.ai.data.local.dao.ReverseGeocodingCacheDao
import com.kisanseva.ai.data.mapper.fromCurrentWeatherCacheEntity
import com.kisanseva.ai.data.mapper.fromForecastCacheEntity
import com.kisanseva.ai.data.mapper.fromReverseGeocodingCacheEntity
import com.kisanseva.ai.data.mapper.toCurrentWeatherCacheEntity
import com.kisanseva.ai.data.mapper.toForecastCacheEntity
import com.kisanseva.ai.data.mapper.toReverseGeocodingCacheEntity
import com.kisanseva.ai.data.remote.WeatherApi
import com.kisanseva.ai.domain.model.CurrentWeatherResponse
import com.kisanseva.ai.domain.model.ForecastResponse
import com.kisanseva.ai.domain.model.GeocodingResponse
import com.kisanseva.ai.domain.repository.WeatherRepository
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi,
    private val currentWeatherCacheDao: CurrentWeatherCacheDao,
    private val forecastCacheDao: ForecastCacheDao,
    private val reverseGeocodingCacheDao: ReverseGeocodingCacheDao
) : WeatherRepository {

    private val weatherCacheDuration = 3600 * 1000 // 1 hour
    private val geocodingCacheDuration = 24 * 3600 * 1000 // 24 hours

    override suspend fun getCurrentWeather(lat: Double, lon: Double): CurrentWeatherResponse {
        val cacheId = "$lat,$lon"
        val cached = currentWeatherCacheDao.get(cacheId)

        if (cached != null && (System.currentTimeMillis() - cached.timestamp < weatherCacheDuration)) {
            return fromCurrentWeatherCacheEntity(cached)
        }

        val remoteData = weatherApi.getCurrentWeather(lat, lon)
        currentWeatherCacheDao.insert(toCurrentWeatherCacheEntity(remoteData, lat, lon))
        return remoteData
    }

    override suspend fun getWeatherForecast(lat: Double, lon: Double): ForecastResponse {
        val cacheId = "$lat,$lon"
        val cached = forecastCacheDao.get(cacheId)

        if (cached != null && (System.currentTimeMillis() - cached.timestamp < weatherCacheDuration)) {
            return fromForecastCacheEntity(cached)
        }

        val remoteData = weatherApi.getWeatherForecast(lat, lon)
        forecastCacheDao.insert(toForecastCacheEntity(remoteData, lat, lon))
        return remoteData
    }

    override suspend fun getReverseGeocoding(lat: Double, lon: Double): List<GeocodingResponse> {
        val cacheId = "$lat,$lon"
        val cached = reverseGeocodingCacheDao.get(cacheId)

        if (cached != null && (System.currentTimeMillis() - cached.timestamp < geocodingCacheDuration)) {
            return fromReverseGeocodingCacheEntity(cached)
        }

        val remoteData = weatherApi.getReverseGeocoding(lat, lon)
        reverseGeocodingCacheDao.insert(toReverseGeocodingCacheEntity(remoteData, lat, lon))
        return remoteData
    }
}
