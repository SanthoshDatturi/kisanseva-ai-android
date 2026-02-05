package com.kisanseva.ai.data.mapper

import com.kisanseva.ai.data.local.entity.MessageEntity
import com.kisanseva.ai.data.local.entity.PartEntity
import com.kisanseva.ai.domain.model.Content
import com.kisanseva.ai.domain.model.FileData
import com.kisanseva.ai.domain.model.Message
import com.kisanseva.ai.domain.model.Part

fun Message.toEntity(): MessageEntity {
    return MessageEntity(
        id = id,
        chatId = chatId,
        ts = ts,
        role = content.role,
        parts = content.parts?.map { it.toEntity() } ?: emptyList()
    )
}

fun Part.toEntity(): PartEntity {
    return PartEntity(
        text = text,
        fileUri = fileData?.fileUri,
        mimeType = fileData?.mimeType,
        localUri = fileData?.localUri
    )
}

fun MessageEntity.toDomain(): Message {
    return Message(
        id = id,
        chatId = chatId,
        content = Content(
            role = role,
            parts = parts.map { it.toDomain() }
        ),
        ts = ts
    )
}

fun PartEntity.toDomain(): Part {
    val fileData = if (fileUri != null || mimeType != null || localUri != null) {
        FileData(
            fileUri = fileUri,
            mimeType = mimeType,
            localUri = localUri
        )
    } else {
        null
    }
    return Part(
        text = text,
        fileData = fileData
    )
}
