package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.OTPSendRequest
import com.kisanseva.ai.domain.model.OTPStatusResponse
import com.kisanseva.ai.domain.model.OTPVerifyRequest
import com.kisanseva.ai.domain.model.Token
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class AuthApi(
    private val client: OkHttpClient,
    baseUrl: String,
    private val json: Json
) {

    private val authUrl = "$baseUrl/auth"

    suspend fun sendOtp(request: OTPSendRequest): Result<OTPStatusResponse, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val requestBodyJson = json.encodeToString(OTPSendRequest.serializer(), request)
                val body = requestBodyJson.toRequestBody("application/json".toMediaType())
                val url = "$authUrl/send-otp"
                val httpRequest = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()
                client.newCall(httpRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.Error(response.code.toNetworkError())
                    }
                    val responseBody = response.body.string()
                    Result.Success(json.decodeFromString(OTPStatusResponse.serializer(), responseBody))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }

    suspend fun verifyOtp(request: OTPVerifyRequest): Result<Token, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val requestBodyJson = json.encodeToString(OTPVerifyRequest.serializer(), request)
                val body = requestBodyJson.toRequestBody("application/json".toMediaType())

                val httpRequest = Request.Builder()
                    .url("$authUrl/verify-otp")
                    .post(body)
                    .build()

                client.newCall(httpRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.Error(response.code.toNetworkError())
                    }
                    val responseBody = response.body.string()
                    Result.Success(json.decodeFromString(Token.serializer(), responseBody))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }
}
