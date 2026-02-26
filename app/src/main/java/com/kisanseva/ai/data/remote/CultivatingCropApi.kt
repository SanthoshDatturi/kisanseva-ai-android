package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.model.CultivatingCrop
import com.kisanseva.ai.domain.model.IntercroppingDetails
import com.kisanseva.ai.exception.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class CultivatingCropApi(
    private val client: OkHttpClient,
    private val baseUrl: String,
    private val json: Json
) {
    private val cultivatingCropsUrl = "$baseUrl/cultivating-crops"

    suspend fun getAllCultivatingCrops(): List<CultivatingCrop> =
        withContext(Dispatchers.IO) {
            val url = "$cultivatingCropsUrl/"
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
                    ListSerializer(CultivatingCrop.serializer()),
                    responseBody
                )
            }
        }

    suspend fun getCultivatingCropsByFarmId(farmId: String): List<CultivatingCrop> =
        withContext(Dispatchers.IO) {
            val url = "$cultivatingCropsUrl/farm/$farmId"
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
                    ListSerializer(CultivatingCrop.serializer()),
                    responseBody
                )
            }
        }

    suspend fun getCultivatingCropById(cultivatingCropId: String): CultivatingCrop =
        withContext(Dispatchers.IO) {
            val url = "$cultivatingCropsUrl/$cultivatingCropId"
            val httpRequest = Request.Builder()
                .url(url)
                .get()
                .build()
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ApiException(response.code, response.message)
                }
                val responseBody = response.body.string()
                return@withContext json.decodeFromString(CultivatingCrop.serializer(), responseBody)
            }
        }

    suspend fun deleteCultivatingCrop(cultivatingCropId: String) {
        withContext(Dispatchers.IO) {
            val url = "$cultivatingCropsUrl/$cultivatingCropId"
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

    suspend fun getIntercroppingDetailsById(intercroppingDetailsId: String): IntercroppingDetails =
        withContext(Dispatchers.IO) {
            val url = "$cultivatingCropsUrl/intercropping/$intercroppingDetailsId"
            val httpRequest = Request.Builder()
                .url(url)
                .get()
                .build()
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ApiException(response.code, response.message)
                }
                val responseBody = response.body.string()
                return@withContext json.decodeFromString(IntercroppingDetails.serializer(), responseBody)
            }
        }
}
