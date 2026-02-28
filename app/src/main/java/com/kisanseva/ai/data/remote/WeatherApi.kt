package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.CurrentWeatherResponse
import com.kisanseva.ai.domain.model.ForecastResponse
import com.kisanseva.ai.domain.model.GeocodingResponse
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class WeatherApi(
    private val client: OkHttpClient,
    baseUrl: String,
    private val json: Json
) {
    private val weatherUrl = "$baseUrl/weather"

    suspend fun getCurrentWeather(lat: Double, lon: Double): Result<CurrentWeatherResponse, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$weatherUrl/current?lat=$lat&lon=$lon"
                val httpRequest = Request.Builder()
                    .url(url)
                    .get()
                    .build()
                client.newCall(httpRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.Error(response.code.toNetworkError())
                    }
                    val responseBody = response.body.string()
                    Result.Success(json.decodeFromString(CurrentWeatherResponse.serializer(), responseBody))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }

    suspend fun getWeatherForecast(lat: Double, lon: Double): Result<ForecastResponse, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$weatherUrl/forecast?lat=$lat&lon=$lon"
                val httpRequest = Request.Builder()
                    .url(url)
                    .get()
                    .build()
                client.newCall(httpRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.Error(response.code.toNetworkError())
                    }
                    val responseBody = response.body.string()
                    Result.Success(json.decodeFromString(ForecastResponse.serializer(), responseBody))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }

    suspend fun getReverseGeocoding(lat: Double, lon: Double): Result<List<GeocodingResponse>, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$weatherUrl/reverse-geocoding?lat=$lat&lon=$lon"
                val httpRequest = Request.Builder()
                    .url(url)
                    .get()
                    .build()
                client.newCall(httpRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.Error(response.code.toNetworkError())
                    }
                    val responseBody = response.body.string()
                    Result.Success(json.decodeFromString(
                        ListSerializer(GeocodingResponse.serializer()),
                        responseBody
                    ))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }
}
