package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.SoilHealthRecommendations
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class SoilHealthRecommendationApi(
    private val client: OkHttpClient,
    baseUrl: String,
    private val json: Json
) {
    private val recommendationUrl = "$baseUrl/soil-health-recommendations"

    suspend fun getRecommendationById(id: String): Result<SoilHealthRecommendations, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$recommendationUrl/$id"
                val httpRequest = Request.Builder()
                    .url(url)
                    .get()
                    .build()
                client.newCall(httpRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.Error(response.code.toNetworkError())
                    }
                    val responseBody = response.body.string()
                    Result.Success(json.decodeFromString(SoilHealthRecommendations.serializer(), responseBody))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }

    suspend fun getRecommendationsByCropId(cropId: String): Result<List<SoilHealthRecommendations>, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$recommendationUrl/crop/$cropId"
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
                        ListSerializer(SoilHealthRecommendations.serializer()),
                        responseBody
                    ))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }

    suspend fun deleteRecommendation(id: String): Result<Unit, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$recommendationUrl/$id"
                val httpRequest = Request.Builder()
                    .url(url)
                    .delete()
                    .build()
                client.newCall(httpRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.Error(response.code.toNetworkError())
                    }
                    Result.Success(Unit)
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }
}
