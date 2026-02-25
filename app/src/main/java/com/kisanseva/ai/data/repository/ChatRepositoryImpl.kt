package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.local.dao.ChatSessionDao
import com.kisanseva.ai.data.local.dao.MessageDao
import com.kisanseva.ai.data.local.dao.QueuedMessageDao
import com.kisanseva.ai.data.local.entity.QueuedMessageEntity
import com.kisanseva.ai.data.mapper.toDomain
import com.kisanseva.ai.data.mapper.toEntity
import com.kisanseva.ai.data.remote.ChatApi
import com.kisanseva.ai.data.remote.websocket.Actions
import com.kisanseva.ai.data.remote.websocket.WebSocketController
import com.kisanseva.ai.domain.model.ChatSession
import com.kisanseva.ai.domain.model.ChatType
import com.kisanseva.ai.domain.model.CreateChatRequest
import com.kisanseva.ai.domain.model.Message
import com.kisanseva.ai.domain.model.MessageRequest
import com.kisanseva.ai.domain.model.websocketModels.ChatWebSocketEvent
import com.kisanseva.ai.domain.model.websocketModels.FarmSurveyAgentResponse
import com.kisanseva.ai.domain.model.websocketModels.GeneralChatResponse
import com.kisanseva.ai.domain.repository.ChatRepository
import com.kisanseva.ai.system.storage.MediaStorageManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class ChatRepositoryImpl(
    private val chatApi: ChatApi,
    private val webSocketController: WebSocketController,
    private val messageDao: MessageDao,
    private val queuedMessageDao: QueuedMessageDao,
    private val mediaStorageManager: MediaStorageManager,
    private val chatSessionDao: ChatSessionDao
) : ChatRepository {

    override suspend fun createChatSession(chatType: ChatType, dataId: String?): ChatSession {
        val request = CreateChatRequest(chatType, dataId)
        val session = chatApi.createChatSession(request)
        chatSessionDao.insertOrUpdateChatSessions(listOf(session.toEntity()))
        return session
    }

    override suspend fun getChatSessions(): List<ChatSession> {
        val localChatSessions = chatSessionDao.getChatSessions()
        val latestTimestamp = localChatSessions.maxByOrNull { it.ts }?.ts

        try {
            val remoteChatSessions = chatApi.getChatSessions(latestTimestamp)
            if (remoteChatSessions.isNotEmpty()) {
                chatSessionDao.insertOrUpdateChatSessions(remoteChatSessions.map { it.toEntity() })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return chatSessionDao.getChatSessions().map { it.toDomain() }
    }

    override suspend fun getChatSession(chatId: String): ChatSession {
        return chatApi.getChatSession(chatId)
    }

    override suspend fun getChatMessages(chatId: String): List<Message> {
        val localMessages = messageDao.getMessages(chatId)
        val latestTimestamp = localMessages.maxByOrNull { it.ts }?.ts

        try {
            val remoteMessages = chatApi.getChatMessages(chatId, latestTimestamp, limit = 10)
            if (remoteMessages.isNotEmpty()) {
                remoteMessages.forEach { saveMessage(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return messageDao.getMessages(chatId).map { it.toDomain() }
    }

    override suspend fun deleteChatSession(chatId: String) {
        try {
            chatApi.deleteChatSession(chatId)
        } finally {
            messageDao.deleteMessages(chatId)
            chatSessionDao.deleteChatSession(chatId)
        }
    }

    override fun observeWebSocketEvents(): Flow<ChatWebSocketEvent> {
        return webSocketController.messages.mapNotNull {
            when (it.action) {
                Actions.FARM_SURVEY_AGENT -> {
                    val data = it.data as? FarmSurveyAgentResponse
                    data?.let { response ->
                        ChatWebSocketEvent.FarmSurveyEventChat(
                            command = response.command,
                            farmProfile = response.farmProfile,
                            userMessage = response.userMessage,
                            modelMessage = response.modelMessage
                        )
                    }
                }

                Actions.GENERAL_CHAT -> {
                    val data = it.data as? GeneralChatResponse
                    data?.let { response ->
                        ChatWebSocketEvent.GeneralChatEventChat(
                            command = response.command,
                            userMessage = response.userMessage,
                            modelMessage = response.modelMessage
                        )
                    }
                }

                else -> null
            }
        }.map { event ->
            val savedModelMessage = saveMessage(event.modelMessage)
            when (event) {
                is ChatWebSocketEvent.FarmSurveyEventChat -> {
                    event.copy(modelMessage = savedModelMessage)
                }
                is ChatWebSocketEvent.GeneralChatEventChat -> {
                    event.copy(modelMessage = savedModelMessage)
                }
            }
        }
    }

    override suspend fun sendMessage(action: String, data: MessageRequest) {
        if (webSocketController.isConnected()) {
            webSocketController.sendMessage(action, data)
        } else {
            val queuedMessage = QueuedMessageEntity(
                action = action,
                data = webSocketController.json.encodeToString(data)
            )
            queuedMessageDao.insertMessage(queuedMessage)
        }
    }

    override suspend fun saveMessage(message: Message): Message {
        val entity = message.toEntity()
        val existingEntity = messageDao.getMessageById(message.id)

        val updatedParts = entity.parts.map { part ->
            if (part.fileUri != null && part.localUri == null) {
                val existingPart = existingEntity?.parts?.find { it.fileUri == part.fileUri }
                if (existingPart?.localUri != null) {
                    part.copy(localUri = existingPart.localUri)
                } else {
                    val localFile = mediaStorageManager.downloadToExternalStorage(
                        part.fileUri,
                        part.mimeType
                    )
                    if (localFile != null) {
                        part.copy(localUri = localFile.absolutePath)
                    } else {
                        part
                    }
                }
            } else {
                part
            }
        }

        val updatedEntity = entity.copy(parts = updatedParts)
        messageDao.insertMessage(updatedEntity)
        return updatedEntity.toDomain()
    }

    override suspend fun deleteMessage(messageId: String) {
        messageDao.deleteMessageById(messageId)
    }

    override suspend fun sendQueuedMessages() {
        val queuedMessages = queuedMessageDao.getQueuedMessages().first()
        for (message in queuedMessages) {
            val data = webSocketController.json.decodeFromString<MessageRequest>(message.data)
            webSocketController.sendMessage(message.action, data)
            queuedMessageDao.deleteMessage(message.id)
        }
    }
}
