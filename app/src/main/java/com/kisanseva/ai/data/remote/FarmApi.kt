package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.FarmProfile
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class FarmApi(
    private val client: OkHttpClient,
    baseUrl: String,
    private val json: Json
) {
    private val farmUrl = "$baseUrl/farm-profiles"

    suspend fun createOrUpdateFarmProfile(profile: FarmProfile): Result<FarmProfile, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$farmUrl/"
                val requestBody = json.encodeToString(FarmProfile.serializer(), profile)
                val httpRequest = Request.Builder()
                    .url(url)
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()
                client.newCall(httpRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.Error(response.code.toNetworkError())
                    }
                    val responseBody = response.body.string()
                    Result.Success(json.decodeFromString(FarmProfile.serializer(), responseBody))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }

    suspend fun getProfileById(farmId: String): Result<FarmProfile, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$farmUrl/$farmId"
                val httpRequest = Request.Builder()
                    .url(url)
                    .get()
                    .build()
                client.newCall(httpRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.Error(response.code.toNetworkError())
                    }
                    val responseBody = response.body.string()
                    Result.Success(json.decodeFromString(FarmProfile.serializer(), responseBody))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }

    suspend fun getFarmProfiles(): Result<List<FarmProfile>, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$farmUrl/"
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
                        ListSerializer(FarmProfile.serializer()),
                        responseBody
                    ))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }

    suspend fun deleteProfile(farmId: String): Result<Unit, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$farmUrl/$farmId"
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
