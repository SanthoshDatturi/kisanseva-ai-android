package com.kisanseva.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kisanseva.ai.domain.model.ChatType

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val chatType: ChatType,
    val dataId: String? = null,
    val ts: Double
)
