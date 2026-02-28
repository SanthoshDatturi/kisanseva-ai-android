package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.CultivationCalendar
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class CultivatingCalendarApi(
    private val client: OkHttpClient,
    baseUrl: String,
    private val json: Json
) {
    private val calendarUrl = "$baseUrl/cultivation-calendars"

    suspend fun getCalendarById(calendarId: String): Result<CultivationCalendar, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$calendarUrl/$calendarId"
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
                        CultivationCalendar.serializer(),
                        responseBody
                    ))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }

    suspend fun getCalendarByCropId(cropId: String): Result<CultivationCalendar, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$calendarUrl/crop/$cropId"
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
                        CultivationCalendar.serializer(),
                        responseBody
                    ))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }

    suspend fun deleteCalendar(calendarId: String): Result<Unit, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$calendarUrl/$calendarId"
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
