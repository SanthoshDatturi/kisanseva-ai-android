package com.kisanseva.ai.data.mapper

import com.kisanseva.ai.data.local.entity.ChatSessionEntity
import com.kisanseva.ai.domain.model.ChatSession

fun ChatSessionEntity.toDomain(): ChatSession {
    return ChatSession(
        id = id,
        userId = userId,
        chatType = chatType,
        dataId = dataId,
        ts = ts
    )
}

fun ChatSession.toEntity(): ChatSessionEntity {
    return ChatSessionEntity(
        id = id,
        userId = userId,
        chatType = chatType,
        dataId = dataId,
        ts = ts
    )
}