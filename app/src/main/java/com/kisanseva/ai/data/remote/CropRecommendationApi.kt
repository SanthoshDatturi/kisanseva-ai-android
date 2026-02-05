package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.model.CropRecommendationResponse
import com.kisanseva.ai.exception.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class CropRecommendationApi(
    private val client: OkHttpClient,
    baseUrl: String,
    private val json: Json
) {
    private val recommendationUrl = "$baseUrl/crop-recommendations"

    suspend fun getCropRecommendationByFarmId(farmId: String): CropRecommendationResponse =
        withContext(Dispatchers.IO) {
            val url = "$recommendationUrl/farm/$farmId"
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
                    CropRecommendationResponse.serializer(),
                    responseBody
                )
            }
        }

    suspend fun getCropRecommendationById(recommendationId: String): CropRecommendationResponse =
        withContext(Dispatchers.IO) {
            val url = "$recommendationUrl/$recommendationId"
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
                    CropRecommendationResponse.serializer(),
                    responseBody
                )
            }
        }
}