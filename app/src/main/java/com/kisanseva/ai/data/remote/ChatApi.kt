package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.ChatSession
import com.kisanseva.ai.domain.model.CreateChatRequest
import com.kisanseva.ai.domain.model.Message
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ChatApi(
    private val client: OkHttpClient,
    baseUrl: String,
    private val json: Json
) {
    private val chatUrl = "$baseUrl/chats"

    suspend fun createChatSession(request: CreateChatRequest): Result<ChatSession, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val httpRequest = Request.Builder()
                    .url(chatUrl)
                    .post(json.encodeToString(CreateChatRequest.serializer(), request).toRequestBody("application/json".toMediaType()))
                    .build()
                client.newCall(httpRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.Error(response.code.toNetworkError())
                    }
                    val responseBody = response.body.string()
                    Result.Success(json.decodeFromString(ChatSession.serializer(), responseBody))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }

    suspend fun getChatSessions(timestamp: Double? = null): Result<List<ChatSession>, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val urlBuilder = "$chatUrl/".toHttpUrl().newBuilder()
                timestamp?.let { urlBuilder.addQueryParameter("timestamp", it.toString()) }
                val httpRequest = Request.Builder()
                    .url(urlBuilder.build())
                    .get()
                    .build()
                client.newCall(httpRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.Error(response.code.toNetworkError())
                    }
                    val responseBody = response.body.string()
                    Result.Success(json.decodeFromString(ListSerializer(ChatSession.serializer()), responseBody))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }

    suspend fun getChatSession(chatId: String): Result<ChatSession, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$chatUrl/$chatId"
                val httpRequest = Request.Builder()
                    .url(url)
                    .get()
                    .build()
                client.newCall(httpRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.Error(response.code.toNetworkError())
                    }
                    val responseBody = response.body.string()
                    Result.Success(json.decodeFromString(ChatSession.serializer(), responseBody))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }

    suspend fun getChatMessages(
        chatId: String,
        timestamp: Double? = null,
        limit: Int? = null
    ): Result<List<Message>, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val urlBuilder = "$chatUrl/$chatId/messages".toHttpUrl().newBuilder()
                timestamp?.let { urlBuilder.addQueryParameter("timestamp", it.toString()) }
                limit?.let { urlBuilder.addQueryParameter("limit", it.toString()) }
                val httpRequest = Request.Builder()
                    .url(urlBuilder.build())
                    .get()
                    .build()
                client.newCall(httpRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.Error(response.code.toNetworkError())
                    }
                    val responseBody = response.body.string()
                    Result.Success(json.decodeFromString(ListSerializer(Message.serializer()), responseBody))
                }
            } catch (_: IOException) {
                Result.Error(DataError.Network.NO_INTERNET)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }

    suspend fun deleteChatSession(chatId: String): Result<Unit, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$chatUrl/$chatId"
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
