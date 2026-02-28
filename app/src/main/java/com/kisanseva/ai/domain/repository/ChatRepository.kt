package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.ChatSession
import com.kisanseva.ai.domain.model.ChatType
import com.kisanseva.ai.domain.model.Message
import com.kisanseva.ai.domain.model.MessageRequest
import com.kisanseva.ai.domain.model.websocketModels.ChatWebSocketEvent
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun createChatSession(chatType: ChatType, dataId: String? = null): Result<ChatSession, DataError.Network>
    fun getChatSessions(): Flow<List<ChatSession>>
    fun getChatMessages(chatId: String): Flow<List<Message>>
    suspend fun getChatSession(chatId: String): Result<ChatSession, DataError.Network>
    suspend fun deleteChatSession(chatId: String): Result<Unit, DataError.Network>

    fun observeWebSocketEvents(): Flow<ChatWebSocketEvent>
    suspend fun sendMessage(action: String, data: MessageRequest): Result<Unit, DataError.Network>
    
    suspend fun saveMessage(message: Message): Message
    suspend fun deleteMessage(messageId: String)
    suspend fun sendQueuedMessages()
    suspend fun refreshChatSessions(): Result<Unit, DataError.Network>
    suspend fun refreshChatMessages(chatId: String): Result<Unit, DataError.Network>
}
