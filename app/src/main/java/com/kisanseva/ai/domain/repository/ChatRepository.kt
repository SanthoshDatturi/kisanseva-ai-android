package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.model.ChatSession
import com.kisanseva.ai.domain.model.ChatType
import com.kisanseva.ai.domain.model.Message
import com.kisanseva.ai.domain.model.MessageRequest
import com.kisanseva.ai.domain.model.websocketModels.ChatWebSocketEvent
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun createChatSession(chatType: ChatType, dataId: String? = null): ChatSession
    fun getChatSessions(): Flow<List<ChatSession>>
    fun getChatMessages(chatId: String): Flow<List<Message>>
    suspend fun getChatSession(chatId: String): ChatSession
    suspend fun deleteChatSession(chatId: String)

    fun observeWebSocketEvents(): Flow<ChatWebSocketEvent>
    suspend fun sendMessage(action: String, data: MessageRequest)
    
    suspend fun saveMessage(message: Message): Message
    suspend fun deleteMessage(messageId: String)
    suspend fun sendQueuedMessages()
    suspend fun refreshChatSessions()
    suspend fun refreshChatMessages(chatId: String)
}
