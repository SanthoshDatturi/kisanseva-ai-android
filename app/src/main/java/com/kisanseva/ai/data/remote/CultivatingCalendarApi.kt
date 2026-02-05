package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.model.CultivationCalendar
import com.kisanseva.ai.exception.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class CultivatingCalendarApi(
    private val client: OkHttpClient,
    baseUrl: String,
    private val json: Json
) {
    private val calendarUrl = "$baseUrl/cultivation-calendars"

    suspend fun getCalendarById(calendarId: String): CultivationCalendar =
        withContext(Dispatchers.IO) {
            val url = "$calendarUrl/$calendarId"
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
                    CultivationCalendar.serializer(),
                    responseBody
                )
            }
        }

    suspend fun getCalendarByCropId(cropId: String): CultivationCalendar =
        withContext(Dispatchers.IO) {
            val url = "$calendarUrl/crop/$cropId"
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
                    CultivationCalendar.serializer(),
                    responseBody
                )
            }
        }

    suspend fun deleteCalendar(calendarId: String) {
        withContext(Dispatchers.IO) {
            val url = "$calendarUrl/$calendarId"
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