package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.model.User
import com.kisanseva.ai.exception.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class UserApi(
    private val client: OkHttpClient,
    baseUrl: String,
    private val json: Json
) {
    private val authUrl = "$baseUrl/auth"

    suspend fun getProfile(): User =
        withContext(Dispatchers.IO) {
            val url = "$authUrl/user"
            val httpRequest = Request.Builder()
                .url(url)
                .get()
                .build()
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ApiException(response.code, response.message)
                }
                val responseBody = response.body.string()
                return@withContext json.decodeFromString(User.serializer(), responseBody)
            }
        }
}
