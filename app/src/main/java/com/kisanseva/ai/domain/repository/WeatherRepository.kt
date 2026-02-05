package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.model.CurrentWeatherResponse
import com.kisanseva.ai.domain.model.ForecastResponse
import com.kisanseva.ai.domain.model.GeocodingResponse

interface WeatherRepository {
    suspend fun getCurrentWeather(lat: Double, lon: Double): CurrentWeatherResponse
    suspend fun getWeatherForecast(lat: Double, lon: Double): ForecastResponse
    suspend fun getReverseGeocoding(lat: Double, lon: Double): List<GeocodingResponse>
}
