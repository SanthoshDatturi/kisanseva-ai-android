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
import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.ChatSession
import com.kisanseva.ai.domain.model.ChatType
import com.kisanseva.ai.domain.model.CreateChatRequest
import com.kisanseva.ai.domain.model.Message
import com.kisanseva.ai.domain.model.MessageRequest
import com.kisanseva.ai.domain.model.websocketModels.ChatWebSocketEvent
import com.kisanseva.ai.domain.model.websocketModels.FarmSurveyAgentResponse
import com.kisanseva.ai.domain.model.websocketModels.GeneralChatResponse
import com.kisanseva.ai.domain.repository.ChatRepository
import com.kisanseva.ai.domain.state.Result
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

    override suspend fun createChatSession(chatType: ChatType, dataId: String?): Result<ChatSession, DataError.Network> {
        val request = CreateChatRequest(chatType, dataId)
        return when (val result = chatApi.createChatSession(request)) {
            is Result.Error -> Result.Error(result.error)
            is Result.Success -> {
                chatSessionDao.insertOrUpdateChatSessions(listOf(result.data.toEntity()))
                Result.Success(result.data)
            }
        }
    }

    override fun getChatSessions(): Flow<List<ChatSession>> {
        return chatSessionDao.getChatSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshChatSessions(): Result<Unit, DataError.Network> {
        val localChatSessions = chatSessionDao.getChatSessions().first()
        val latestTimestamp = localChatSessions.maxByOrNull { it.ts }?.ts
        return when (val result = chatApi.getChatSessions(latestTimestamp)) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                if (result.data.isNotEmpty()) {
                    chatSessionDao.insertOrUpdateChatSessions(result.data.map { it.toEntity() })
                }
                Result.Success(Unit)
            }
        }
    }

    override suspend fun getChatSession(chatId: String): Result<ChatSession, DataError.Network> {
        return chatApi.getChatSession(chatId)
    }

    override fun getChatMessages(chatId: String): Flow<List<Message>> {
        return messageDao.getMessagesFlow(chatId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshChatMessages(chatId: String): Result<Unit, DataError.Network> {
        val localMessages = messageDao.getMessages(chatId)
        val latestTimestamp = localMessages.maxByOrNull { it.ts }?.ts
        return when (val result = chatApi.getChatMessages(chatId, latestTimestamp, limit = 50)) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                if (result.data.isNotEmpty()) {
                    result.data.forEach { saveMessage(it) }
                }
                Result.Success(Unit)
            }
        }
    }

    override suspend fun deleteChatSession(chatId: String): Result<Unit, DataError.Network> {
        return when (val result = chatApi.deleteChatSession(chatId)) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                messageDao.deleteMessages(chatId)
                chatSessionDao.deleteChatSession(chatId)
                Result.Success(Unit)
            }
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

    override suspend fun sendMessage(action: String, data: MessageRequest): Result<Unit, DataError.Network> {
        return if (webSocketController.isConnected()) {
            try {
                webSocketController.sendMessage(action, data)
                Result.Success(Unit)
            } catch (_: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        } else {
            val queuedMessage = QueuedMessageEntity(
                action = action,
                data = webSocketController.json.encodeToString(data)
            )
            queuedMessageDao.insertMessage(queuedMessage)
            Result.Error(DataError.Network.NO_INTERNET)
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
