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
import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.CurrentWeatherResponse
import com.kisanseva.ai.domain.model.ForecastResponse
import com.kisanseva.ai.domain.model.GeocodingResponse
import com.kisanseva.ai.domain.repository.WeatherRepository
import com.kisanseva.ai.domain.state.Result
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi,
    private val currentWeatherCacheDao: CurrentWeatherCacheDao,
    private val forecastCacheDao: ForecastCacheDao,
    private val reverseGeocodingCacheDao: ReverseGeocodingCacheDao
) : WeatherRepository {

    private val weatherCacheDuration = 3600 * 1000 // 1 hour
    private val geocodingCacheDuration = 24 * 3600 * 1000 // 24 hours

    override suspend fun getCurrentWeather(lat: Double, lon: Double): Result<CurrentWeatherResponse, DataError.Network> {
        val cacheId = "$lat,$lon"
        val cached = currentWeatherCacheDao.get(cacheId)

        if (cached != null && (System.currentTimeMillis() - cached.timestamp < weatherCacheDuration)) {
            return Result.Success(fromCurrentWeatherCacheEntity(cached))
        }

        return when (val result = weatherApi.getCurrentWeather(lat, lon)) {
            is Result.Error -> {
                if (cached != null) Result.Success(fromCurrentWeatherCacheEntity(cached))
                else Result.Error(result.error)
            }
            is Result.Success -> {
                currentWeatherCacheDao.insert(toCurrentWeatherCacheEntity(result.data, lat, lon))
                Result.Success(result.data)
            }
        }
    }

    override suspend fun getWeatherForecast(lat: Double, lon: Double): Result<ForecastResponse, DataError.Network> {
        val cacheId = "$lat,$lon"
        val cached = forecastCacheDao.get(cacheId)

        if (cached != null && (System.currentTimeMillis() - cached.timestamp < weatherCacheDuration)) {
            return Result.Success(fromForecastCacheEntity(cached))
        }

        return when (val result = weatherApi.getWeatherForecast(lat, lon)) {
            is Result.Error -> {
                if (cached != null) Result.Success(fromForecastCacheEntity(cached))
                else Result.Error(result.error)
            }
            is Result.Success -> {
                forecastCacheDao.insert(toForecastCacheEntity(result.data, lat, lon))
                Result.Success(result.data)
            }
        }
    }

    override suspend fun getReverseGeocoding(lat: Double, lon: Double): Result<List<GeocodingResponse>, DataError.Network> {
        val cacheId = "$lat,$lon"
        val cached = reverseGeocodingCacheDao.get(cacheId)

        if (cached != null && (System.currentTimeMillis() - cached.timestamp < geocodingCacheDuration)) {
            return Result.Success(fromReverseGeocodingCacheEntity(cached))
        }

        return when (val result = weatherApi.getReverseGeocoding(lat, lon)) {
            is Result.Error -> {
                if (cached != null) Result.Success(fromReverseGeocodingCacheEntity(cached))
                else Result.Error(result.error)
            }
            is Result.Success -> {
                reverseGeocodingCacheDao.insert(toReverseGeocodingCacheEntity(result.data, lat, lon))
                Result.Success(result.data)
            }
        }
    }
}
