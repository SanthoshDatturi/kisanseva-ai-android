package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.model.SoilHealthRecommendations
import com.kisanseva.ai.exception.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class SoilHealthRecommendationApi(
    private val client: OkHttpClient,
    baseUrl: String,
    private val json: Json
) {
    private val recommendationUrl = "$baseUrl/soil-health-recommendations"

    suspend fun getRecommendationById(id: String): SoilHealthRecommendations =
        withContext(Dispatchers.IO) {
            val url = "$recommendationUrl/$id"
            val httpRequest = Request.Builder()
                .url(url)
                .get()
                .build()
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ApiException(response.code, response.message)
                }
                val responseBody = response.body.string()
                return@withContext json.decodeFromString(SoilHealthRecommendations.serializer(), responseBody)
            }
        }

    suspend fun getRecommendationsByCropId(cropId: String): List<SoilHealthRecommendations> =
        withContext(Dispatchers.IO) {
            val url = "$recommendationUrl/crop/$cropId"
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
                    ListSerializer(SoilHealthRecommendations.serializer()),
                    responseBody
                )
            }
        }

    suspend fun deleteRecommendation(id: String) =
        withContext(Dispatchers.IO) {
            val url = "$recommendationUrl/$id"
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
}
