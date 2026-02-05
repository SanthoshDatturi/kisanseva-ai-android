package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.model.InvestmentBreakdown
import com.kisanseva.ai.exception.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class InvestmentBreakdownApi(
    private val client: OkHttpClient,
    baseUrl: String,
    private val json: Json
) {
    private val breakdownUrl = "$baseUrl/investment-breakdowns"

    suspend fun getBreakdownById(id: String): InvestmentBreakdown =
        withContext(Dispatchers.IO) {
            val url = "$breakdownUrl/$id"
            val httpRequest = Request.Builder()
                .url(url)
                .get()
                .build()
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ApiException(response.code, response.message)
                }
                val responseBody = response.body.string()
                return@withContext json.decodeFromString(InvestmentBreakdown.serializer(), responseBody)
            }
        }

    suspend fun getBreakdownByCropId(cropId: String): InvestmentBreakdown =
        withContext(Dispatchers.IO) {
            val url = "$breakdownUrl/crop/$cropId"
            val httpRequest = Request.Builder()
                .url(url)
                .get()
                .build()
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ApiException(response.code, response.message)
                }
                val responseBody = response.body.string()
                return@withContext json.decodeFromString(InvestmentBreakdown.serializer(), responseBody)
            }
        }

    suspend fun deleteBreakdown(id: String) =
        withContext(Dispatchers.IO) {
            val url = "$breakdownUrl/$id"
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
