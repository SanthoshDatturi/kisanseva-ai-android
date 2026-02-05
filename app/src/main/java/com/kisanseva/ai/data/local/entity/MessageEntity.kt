package com.kisanseva.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val ts: Double,
    val role: String?,
    val parts: List<PartEntity>
)

@Serializable
data class PartEntity(
    val text: String? = null,
    val fileUri: String? = null,
    val mimeType: String? = null,
    val localUri: String? = null
)
