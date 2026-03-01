package com.kisanseva.ai.data.mapper

import com.kisanseva.ai.data.local.entity.ChatSessionEntity
import com.kisanseva.ai.domain.model.ChatSession
import com.kisanseva.ai.domain.model.LastUserMessageState

fun ChatSessionEntity.toDomain(): ChatSession {
    return ChatSession(
        id = id,
        userId = userId,
        chatType = chatType,
        dataId = dataId,
        title = title,
        ts = ts,
        lastUserMessageState = if (lastUserMessageRequestId != null && lastUserMessageState != null) {
            LastUserMessageState(lastUserMessageRequestId, lastUserMessageState)
        } else null
    )
}

fun ChatSession.toEntity(): ChatSessionEntity {
    return ChatSessionEntity(
        id = id,
        userId = userId,
        chatType = chatType,
        dataId = dataId,
        title = title,
        ts = ts,
        lastUserMessageRequestId = lastUserMessageState?.requestId,
        lastUserMessageState = lastUserMessageState?.state
    )
}
