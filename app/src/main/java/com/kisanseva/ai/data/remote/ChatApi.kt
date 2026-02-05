package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.model.ChatSession
import com.kisanseva.ai.domain.model.Message
import com.kisanseva.ai.exception.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class ChatApi(
    private val client: OkHttpClient,
    baseUrl: String,
    private val json: Json
) {
    private val chatUrl = "$baseUrl/chats"

    suspend fun getChatSessions(timestamp: Double? = null): List<ChatSession> =
        withContext(Dispatchers.IO) {
            val urlBuilder = "$chatUrl/".toHttpUrl().newBuilder()
            
            timestamp?.let { urlBuilder.addQueryParameter("timestamp", it.toString()) }
            
            val httpRequest = Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build()
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ApiException(response.code, response.message)
                }
                val responseBody = response.body.string()
                return@withContext json.decodeFromString(
                    ListSerializer(ChatSession.serializer()),
                    responseBody
                )
            }
        }

    suspend fun getChatSession(chatId: String): ChatSession =
        withContext(Dispatchers.IO) {
            val url = "$chatUrl/$chatId"
            val httpRequest = Request.Builder()
                .url(url)
                .get()
                .build()
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ApiException(response.code, response.message)
                }
                val responseBody = response.body.string()
                return@withContext json.decodeFromString(ChatSession.serializer(), responseBody)
            }
        }

    suspend fun getChatMessages(
        chatId: String,
        timestamp: Double? = null,
        limit: Int? = null
    ): List<Message> =
        withContext(Dispatchers.IO) {
            val urlBuilder = "$chatUrl/$chatId/messages".toHttpUrl().newBuilder()
            
            timestamp?.let { urlBuilder.addQueryParameter("timestamp", it.toString()) }
            limit?.let { urlBuilder.addQueryParameter("limit", it.toString()) }
            
            val httpRequest = Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build()
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ApiException(response.code, response.message)
                }
                val responseBody = response.body.string()
                return@withContext json.decodeFromString(
                    ListSerializer(Message.serializer()),
                    responseBody
                )
            }
        }

    suspend fun deleteChatSession(chatId: String) {
        withContext(Dispatchers.IO) {
            val url = "$chatUrl/$chatId"
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
