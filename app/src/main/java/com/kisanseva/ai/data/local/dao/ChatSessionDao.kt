package com.kisanseva.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanseva.ai.data.local.entity.ChatSessionEntity
import com.kisanseva.ai.domain.model.MessageState
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateChatSessions(chatSessions: List<ChatSessionEntity>)

    @Query("SELECT * FROM chat_sessions ORDER BY ts DESC")
    fun getChatSessions(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE id = :chatId")
    fun getChatSession(chatId: String): Flow<ChatSessionEntity?>

    @Query("DELETE FROM chat_sessions WHERE id = :chatId")
    suspend fun deleteChatSession(chatId: String)

    @Query("UPDATE chat_sessions SET lastUserMessageRequestId = :requestId, lastUserMessageState = :state WHERE id = :chatId")
    suspend fun updateSessionState(chatId: String, requestId: String, state: MessageState)
}
