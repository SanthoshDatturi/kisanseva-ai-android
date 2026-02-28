package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.CurrentWeatherResponse
import com.kisanseva.ai.domain.model.ForecastResponse
import com.kisanseva.ai.domain.model.GeocodingResponse
import com.kisanseva.ai.domain.state.Result

interface WeatherRepository {
    suspend fun getCurrentWeather(lat: Double, lon: Double): Result<CurrentWeatherResponse, DataError.Network>
    suspend fun getWeatherForecast(lat: Double, lon: Double): Result<ForecastResponse, DataError.Network>
    suspend fun getReverseGeocoding(lat: Double, lon: Double): Result<List<GeocodingResponse>, DataError.Network>
}
