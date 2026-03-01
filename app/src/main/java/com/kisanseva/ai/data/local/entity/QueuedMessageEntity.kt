package com.kisanseva.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queued_messages")
data class QueuedMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val messageJson: String, // Storing the full BaseWebSocketRequest as JSON string
    val timestamp: Long = System.currentTimeMillis()
)
