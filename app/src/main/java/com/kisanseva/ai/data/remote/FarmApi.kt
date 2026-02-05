package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.model.FarmProfile
import com.kisanseva.ai.exception.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class FarmApi(
    private val client: OkHttpClient,
    private val baseUrl: String,
    private val json: Json
) {
    private val farmUrl = "$baseUrl/farm-profiles"

    suspend fun createOrUpdateFarmProfile(profile: FarmProfile): FarmProfile =
        withContext(Dispatchers.IO) {
            val url = "$farmUrl/"
            val requestBody = json.encodeToString(FarmProfile.serializer(), profile)
            val httpRequest = Request.Builder()
                .url(url)
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ApiException(response.code, response.message)
                }
                val responseBody = response.body.string()
                return@withContext json.decodeFromString(FarmProfile.serializer(), responseBody)
            }
        }

    suspend fun getProfileById(farmId: String): FarmProfile =
        withContext(Dispatchers.IO) {
            val url = "$farmUrl/$farmId"
            val httpRequest = Request.Builder()
                .url(url)
                .get()
                .build()
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ApiException(response.code, response.message)
                }
                val responseBody = response.body.string()
                return@withContext json.decodeFromString(FarmProfile.serializer(), responseBody)
            }
        }

    suspend fun getFarmProfiles(): List<FarmProfile> =
        withContext(Dispatchers.IO) {
            val url = "$farmUrl/"
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
                    ListSerializer(FarmProfile.serializer()),
                    responseBody
                )
            }
        }

    suspend fun deleteProfile(farmId: String) {
        withContext(Dispatchers.IO) {
            val url = "$farmUrl/$farmId"
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
}