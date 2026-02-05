package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.model.PesticideRecommendationResponse
import com.kisanseva.ai.domain.model.PesticideStageUpdateRequest
import com.kisanseva.ai.exception.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class PesticideRecommendationApi(
    private val client: OkHttpClient,
    baseUrl: String,
    private val json: Json
) {
    private val pesticideUrl = "$baseUrl/pesticide-recommendations"

    suspend fun getRecommendationById(recommendationId: String): PesticideRecommendationResponse =
        withContext(Dispatchers.IO) {
            val url = "$pesticideUrl/$recommendationId"
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
                    PesticideRecommendationResponse.serializer(),
                    responseBody
                )
            }
        }

    suspend fun getRecommendationsByCropId(cropId: String): List<PesticideRecommendationResponse> =
        withContext(Dispatchers.IO) {
            val url = "$pesticideUrl/crop/$cropId"
            val httpRequest = Request.Builder()
                .url(url)
                .get()
                .build()
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ApiException(response.code, response.message)
                }
                val responseBody = response.body.string()
                return@withContext json.decodeFromString<List<PesticideRecommendationResponse>>(
                    responseBody
                )
            }
        }

    suspend fun deleteRecommendation(recommendationId: String) =
        withContext(Dispatchers.IO) {
            val url = "$pesticideUrl/$recommendationId"
            val httpRequest = Request.Builder()
                .url(url)
                .delete()
                .build()
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ApiException(response.code, response.message)
                }
            }
        }

    suspend fun updatePesticideStage(recommendationId: String, request: PesticideStageUpdateRequest) =
        withContext(Dispatchers.IO) {
            val url = "$pesticideUrl/$recommendationId/stage"
            val requestBodyJson = json.encodeToString(PesticideStageUpdateRequest.serializer(), request)
            val body = requestBodyJson.toRequestBody("application/json".toMediaType())
            val httpRequest = Request.Builder()
                .url(url)
                .patch(body)
                .build()
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ApiException(response.code, response.message)
                }
            }
        }
}
