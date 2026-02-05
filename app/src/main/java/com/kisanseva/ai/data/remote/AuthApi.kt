package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.model.OTPSendRequest
import com.kisanseva.ai.domain.model.OTPStatusResponse
import com.kisanseva.ai.domain.model.OTPVerifyRequest
import com.kisanseva.ai.domain.model.Token
import com.kisanseva.ai.exception.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class AuthApi(
    private val client: OkHttpClient,
    baseUrl: String,
    private val json: Json
) {

    private val authUrl = "$baseUrl/auth"

    suspend fun sendOtp(request: OTPSendRequest): OTPStatusResponse =
        withContext(Dispatchers.IO) {
            val requestBodyJson = json.encodeToString(OTPSendRequest.serializer(), request)
            val body = requestBodyJson.toRequestBody("application/json".toMediaType())
            val url = "$authUrl/send-otp"
            val httpRequest = Request.Builder()
                .url(url)
                .post(body)
                .build()
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ApiException(response.code, response.message)
                }
                val responseBody = response.body.string()
                return@withContext json.decodeFromString(OTPStatusResponse.serializer(), responseBody)
            }
        }

    suspend fun verifyOtp(request: OTPVerifyRequest): Token =
        withContext(Dispatchers.IO) {
            val requestBodyJson = json.encodeToString(OTPVerifyRequest.serializer(), request)
            val body = requestBodyJson.toRequestBody("application/json".toMediaType())

            val httpRequest = Request.Builder()
                .url("$authUrl/verify-otp")
                .post(body)
                .build()

            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ApiException(response.code, response.message)
                }
                val responseBody = response.body.string()
                return@withContext json.decodeFromString(Token.serializer(), responseBody)
            }
        }
}