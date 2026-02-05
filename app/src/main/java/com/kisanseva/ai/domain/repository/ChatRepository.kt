package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.model.ChatSession
import com.kisanseva.ai.domain.model.Message
import com.kisanseva.ai.domain.model.MessageRequest
import com.kisanseva.ai.domain.model.websocketModels.ChatWebSocketEvent
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun getChatSessions(): List<ChatSession>
    suspend fun getChatSession(chatId: String): ChatSession
    suspend fun getChatMessages(chatId: String): List<Message>
    suspend fun deleteChatSession(chatId: String)

    fun observeWebSocketEvents(): Flow<ChatWebSocketEvent>
    suspend fun sendMessage(action: String, data: MessageRequest)
    
    suspend fun saveMessage(message: Message): Message
    suspend fun deleteMessage(messageId: String)
    suspend fun sendQueuedMessages()
}
