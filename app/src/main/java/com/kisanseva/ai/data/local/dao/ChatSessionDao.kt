package com.kisanseva.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanseva.ai.data.local.entity.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateChatSessions(chatSessions: List<ChatSessionEntity>)

    @Query("SELECT * FROM chat_sessions ORDER BY ts DESC")
    fun getChatSessions(): Flow<List<ChatSessionEntity>>

    @Query("DELETE FROM chat_sessions WHERE id = :chatId")
    suspend fun deleteChatSession(chatId: String)
}
