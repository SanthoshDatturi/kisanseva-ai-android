package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.local.dao.ChatSessionDao
import com.kisanseva.ai.data.local.dao.MessageDao
import com.kisanseva.ai.data.local.entity.MessageEntity
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
import com.kisanseva.ai.domain.model.MessageState
import com.kisanseva.ai.domain.model.Role
import com.kisanseva.ai.domain.model.websocketModels.ChatWebSocketEvent
import com.kisanseva.ai.domain.model.websocketModels.FarmSurveyAgentResponse
import com.kisanseva.ai.domain.model.websocketModels.GeneralChatResponse
import com.kisanseva.ai.domain.model.websocketModels.WorkflowChunkEnvelope
import com.kisanseva.ai.domain.model.websocketModels.WorkflowEvents
import com.kisanseva.ai.domain.repository.ChatRepository
import com.kisanseva.ai.domain.state.Result
import com.kisanseva.ai.system.storage.MediaStorageManager
import com.kisanseva.ai.util.UrlUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

class ChatRepositoryImpl(
    private val chatApi: ChatApi,
    private val webSocketController: WebSocketController,
    private val messageDao: MessageDao,
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
        return when (val result = chatApi.getChatSessions()) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                if (result.data.isNotEmpty()) {
                    chatSessionDao.insertOrUpdateChatSessions(result.data.map { it.toEntity() })
                }
                Result.Success(Unit)
            }
        }
    }

    override suspend fun refreshChatSession(chatId: String): Result<Unit, DataError.Network> {
        return when (val result = chatApi.getChatSession(chatId)) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                chatSessionDao.insertOrUpdateChatSessions(listOf(result.data.toEntity()))
                Result.Success(Unit)
            }
        }
    }

    override fun getChatSession(chatId: String): Flow<ChatSession?> {
        return chatSessionDao.getChatSession(chatId).map { it?.toDomain() }
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
        return webSocketController.messages.mapNotNull { response ->
            val requestId = response.requestId
            when (response.action) {
                Actions.FARM_SURVEY_AGENT -> {
                    val data = response.data as? FarmSurveyAgentResponse
                    data?.let { res ->
                        ChatWebSocketEvent.FarmSurveyEventChat(
                            command = res.command,
                            farmProfile = res.farmProfile,
                            userMessage = res.userMessage?.copy(requestId = requestId ?: res.userMessage.requestId),
                            modelMessage = res.modelMessage.copy(requestId = requestId ?: res.modelMessage.requestId)
                        )
                    }
                }

                Actions.GENERAL_CHAT -> {
                    val data = response.data as? GeneralChatResponse
                    data?.let { res ->
                        ChatWebSocketEvent.GeneralChatEventChat(
                            command = res.command,
                            userMessage = res.userMessage?.copy(requestId = requestId ?: res.userMessage.requestId),
                            modelMessage = res.modelMessage.copy(requestId = requestId ?: res.modelMessage.requestId)
                        )
                    }
                }

                else -> null
            }
        }.map { event ->
            event.userMessage?.let { saveMessage(it) }
            val savedModelMessage = saveMessage(event.modelMessage)
            
            // Update session state to RESOLVED if request IDs match
            event.modelMessage.requestId.let { reqId ->
                chatSessionDao.updateSessionState(
                    chatId = event.modelMessage.chatId,
                    requestId = reqId,
                    state = MessageState.RESOLVED
                )
            }

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

    override fun listenToFarmSurveyProgress(): Flow<String> {
        return webSocketController.workflowEvents
            .filter { it.action == Actions.FARM_SURVEY_AGENT }
            .mapNotNull { event ->
                when (event.event) {
                    WorkflowEvents.STEP_STARTED -> event.step?.let { "Running ${it.readableStep()}..." }
                    WorkflowEvents.WORKFLOW_FAILED -> event.data.extractErrorMessage()?.let { "Failed: $it" }
                    WorkflowEvents.CHUNK -> event.data.toFarmSurveyChunkMessage()
                    else -> null
                }
            }
    }

    override fun listenToGeneralChatProgress(): Flow<String> {
        return webSocketController.workflowEvents
            .filter { it.action == Actions.GENERAL_CHAT }
            .mapNotNull { event ->
                when (event.event) {
                    WorkflowEvents.STEP_STARTED -> event.step?.let { "Running ${it.readableStep()}..." }
                    WorkflowEvents.WORKFLOW_FAILED -> event.data.extractErrorMessage()?.let { "Failed: $it" }
                    WorkflowEvents.CHUNK -> event.data.toGeneralChatChunkMessage()
                    else -> null
                }
            }
    }

    override suspend fun sendMessage(action: String, data: MessageRequest): Result<Unit, DataError.Network> {
        webSocketController.sendMessage(action, data)
        
        // Update session state to SENT
        data.chatId.let { chatId ->
            data.requestId.let { requestId ->
                chatSessionDao.updateSessionState(chatId, requestId, MessageState.SENT)
            }
        }

        return if (webSocketController.isConnected()) {
            Result.Success(Unit)
        } else {
            Result.Error(DataError.Network.NO_INTERNET)
        }
    }

    override suspend fun saveMessage(message: Message): Message {
        val entity = message.toEntity()
        
        // Identify IDs that might be replaced (optimistic IDs)
        val oldIds = mutableListOf<String>()
        if (message.id != null) {
            if (message.content.role == Role.USER.name.lowercase()) {
                if (message.id != message.requestId) oldIds.add(message.requestId)
            } else if (message.content.role == Role.MODEL.name.lowercase()) {
                if (message.id != "${message.requestId}_model") oldIds.add("${message.requestId}_model")
            }
        }

        // Try to find an existing entity to preserve local metadata (like localUri)
        var existingEntity: MessageEntity? = messageDao.getMessageById(entity.id)
        if (existingEntity == null) {
            for (oldId in oldIds) {
                existingEntity = messageDao.getMessageById(oldId)
                if (existingEntity != null) break
            }
        }

        val updatedParts = entity.parts.map { part ->
            if (part.fileUri != null && part.localUri == null) {
                // Recover localUri from existing entity if possible
                val existingPart = existingEntity?.parts?.find { it.fileUri == part.fileUri }
                if (existingPart?.localUri != null) {
                    part.copy(localUri = existingPart.localUri)
                } else {
                    // Download if not found locally
                    val localFile = mediaStorageManager.downloadToExternalStorage(
                        part.fileUri.let { UrlUtils.getFullUrlFromRef(it) },
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
        
        // Insert new entity FIRST to ensure it's in the DB before deleting the old one
        // This prevents the message from vanishing during the process
        messageDao.insertMessage(updatedEntity)
        
        // Now it's safe to delete the old optimistic IDs
        for (oldId in oldIds) {
            if (oldId != updatedEntity.id) {
                messageDao.deleteMessageById(oldId)
            }
        }

        return updatedEntity.toDomain()
    }

    override suspend fun deleteMessage(messageId: String) {
        messageDao.deleteMessageById(messageId)
    }

    override suspend fun sendQueuedMessages() {
        webSocketController.flushQueue()
    }

    private fun String.readableStep(): String = replace("_", " ")

    private fun JsonElement?.toFarmSurveyChunkMessage(): String? {
        if (this == null) return null
        val envelope = runCatching {
            webSocketController.json.decodeFromJsonElement<WorkflowChunkEnvelope>(this)
        }.getOrNull() ?: return null
        val payload = envelope.data as? JsonObject

        return when (envelope.chunkType) {
            "survey_progress" -> {
                val collectedCount = payload?.get("collected_fields")
                    ?.jsonArrayOrNull()
                    ?.size ?: 0
                val missingCount = payload?.get("missing_fields")
                    ?.jsonArrayOrNull()
                    ?.size ?: 0
                when {
                    missingCount > 0 -> "Survey progress: $collectedCount collected, $missingCount pending."
                    else -> "Survey details complete. Finalizing profile."
                }
            }

            "farm_profile_saved" -> {
                val farmId = payload?.get("farm_id")?.jsonPrimitive?.contentOrNull
                if (farmId.isNullOrBlank()) "Farm profile saved." else "Farm profile saved: $farmId"
            }

            else -> null
        }
    }

    private fun JsonElement?.toGeneralChatChunkMessage(): String? {
        if (this == null) return null
        val envelope = runCatching {
            webSocketController.json.decodeFromJsonElement<WorkflowChunkEnvelope>(this)
        }.getOrNull() ?: return null
        val payload = envelope.data as? JsonObject

        return when (envelope.chunkType) {
            "chat_reasoning" -> {
                val intent = payload?.get("user_intent")?.jsonPrimitive?.contentOrNull
                val planSteps = payload?.get("response_plan")?.jsonArrayOrNull()?.size ?: 0
                when {
                    !intent.isNullOrBlank() && planSteps > 0 -> "Intent recognized: $intent ($planSteps plan steps)."
                    !intent.isNullOrBlank() -> "Intent recognized: $intent."
                    planSteps > 0 -> "Response plan prepared with $planSteps steps."
                    else -> "Reasoning completed for this response."
                }
            }

            else -> null
        }
    }

    private fun JsonElement?.extractErrorMessage(): String? {
        val obj = this as? JsonObject ?: return null
        return obj["error"]?.jsonPrimitive?.contentOrNull
    }

    private fun JsonElement.jsonArrayOrNull() = runCatching { this.jsonArray }.getOrNull()
}
