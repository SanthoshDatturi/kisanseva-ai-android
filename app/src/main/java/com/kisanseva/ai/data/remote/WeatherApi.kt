package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.model.CurrentWeatherResponse
import com.kisanseva.ai.domain.model.ForecastResponse
import com.kisanseva.ai.domain.model.GeocodingResponse
import com.kisanseva.ai.exception.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class WeatherApi(
    private val client: OkHttpClient,
    private val baseUrl: String,
    private val json: Json
) {
    private val weatherUrl = "$baseUrl/weather"

    suspend fun getCurrentWeather(lat: Double, lon: Double): CurrentWeatherResponse =
        withContext(Dispatchers.IO) {
            val url = "$weatherUrl/current?lat=$lat&lon=$lon"
            val httpRequest = Request.Builder()
                .url(url)
                .get()
                .build()
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ApiException(response.code, response.message)
                }
                val responseBody = response.body.string()
                return@withContext json.decodeFromString(CurrentWeatherResponse.serializer(), responseBody)
            }
        }

    suspend fun getWeatherForecast(lat: Double, lon: Double): ForecastResponse =
        withContext(Dispatchers.IO) {
            val url = "$weatherUrl/forecast?lat=$lat&lon=$lon"
            val httpRequest = Request.Builder()
                .url(url)
                .get()
                .build()
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ApiException(response.code, response.message)
                }
                val responseBody = response.body.string()
                return@withContext json.decodeFromString(ForecastResponse.serializer(), responseBody)
            }
        }

    suspend fun getReverseGeocoding(lat: Double, lon: Double): List<GeocodingResponse> =
        withContext(Dispatchers.IO) {
            val url = "$weatherUrl/reverse-geocoding?lat=$lat&lon=$lon"
            val httpRequest = Request.Builder()
                .url(url)
                .get()
                .build()
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ApiException(response.code, response.message)
                }
                val responseBody = response.body.string()
                return@withContext json.decodeFromString(
                    ListSerializer(GeocodingResponse.serializer()),
                    responseBody
                )
            }
        }
}
