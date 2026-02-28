package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.InvestmentBreakdown
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class InvestmentBreakdownApi(
    private val client: OkHttpClient,
    baseUrl: String,
    private val json: Json
) {
    private val breakdownUrl = "$baseUrl/investment-breakdowns"

    suspend fun getBreakdownById(id: String): Result<InvestmentBreakdown, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$breakdownUrl/$id"
                val httpRequest = Request.Builder()
                    .url(url)
                    .get()
                    .build()
                client.newCall(httpRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.Error(response.code.toNetworkError())
                    }
                    val responseBody = response.body.string()
                    Result.Success(json.decodeFromString(InvestmentBreakdown.serializer(), responseBody))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }

    suspend fun getBreakdownByCropId(cropId: String): Result<InvestmentBreakdown, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$breakdownUrl/crop/$cropId"
                val httpRequest = Request.Builder()
                    .url(url)
                    .get()
                    .build()
                client.newCall(httpRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.Error(response.code.toNetworkError())
                    }
                    val responseBody = response.body.string()
                    Result.Success(json.decodeFromString(InvestmentBreakdown.serializer(), responseBody))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }

    suspend fun deleteBreakdown(id: String): Result<Unit, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$breakdownUrl/$id"
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
