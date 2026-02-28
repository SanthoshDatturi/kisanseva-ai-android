package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.PesticideRecommendationResponse
import com.kisanseva.ai.domain.model.PesticideStageUpdateRequest
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class PesticideRecommendationApi(
    private val client: OkHttpClient,
    baseUrl: String,
    private val json: Json
) {
    private val pesticideUrl = "$baseUrl/pesticide-recommendations"

    suspend fun getRecommendationById(recommendationId: String): Result<PesticideRecommendationResponse, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$pesticideUrl/$recommendationId"
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
                        PesticideRecommendationResponse.serializer(),
                        responseBody
                    ))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }

    suspend fun getRecommendationsByCropId(cropId: String): Result<List<PesticideRecommendationResponse>, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$pesticideUrl/crop/$cropId"
                val httpRequest = Request.Builder()
                    .url(url)
                    .get()
                    .build()
                client.newCall(httpRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.Error(response.code.toNetworkError())
                    }
                    val responseBody = response.body.string()
                    Result.Success(json.decodeFromString<List<PesticideRecommendationResponse>>(
                        responseBody
                    ))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }

    suspend fun deleteRecommendation(recommendationId: String): Result<Unit, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$pesticideUrl/$recommendationId"
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

    suspend fun updatePesticideStage(recommendationId: String, request: PesticideStageUpdateRequest): Result<Unit, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$pesticideUrl/$recommendationId/stage"
                val requestBodyJson = json.encodeToString(PesticideStageUpdateRequest.serializer(), request)
                val body = requestBodyJson.toRequestBody("application/json".toMediaType())
                val httpRequest = Request.Builder()
                    .url(url)
                    .patch(body)
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
