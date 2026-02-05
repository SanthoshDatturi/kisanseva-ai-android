package com.kisanseva.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.UUID

@Serializable
enum class ChatType {
    @SerialName("farm_survey")
    FARM_SURVEY,
    @SerialName("general")
    GENERAL,
    @SerialName("about_crop")
    ABOUT_CROP,
    @SerialName("about_pests")
    ABOUT_PESTS,
    @SerialName("about_fertilizers")
    ABOUT_FERTILIZERS,
    @SerialName("about_irrigation")
    ABOUT_IRRIGATION
}

@Serializable
enum class Role {
    @SerialName("user")
    USER,
    @SerialName("model")
    MODEL,
    @SerialName("system")
    SYSTEM
}

@Serializable
data class ChatSession(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("chat_type")
    val chatType: ChatType,
    @SerialName("data_id")
    val dataId: String? = null,
    val ts: Double = System.currentTimeMillis() / 1000.0
)

@Serializable
data class Message(
    val id: String,
    @SerialName("chat_id")
    var chatId: String,
    val content: Content,
    val ts: Double = System.currentTimeMillis() / 1000.0
)

@Serializable
data class MessageRequest(
    @SerialName("chat_id")
    val chatId: String? = null,
    val content: Content,
    @SerialName("audio_response")
    val audioResponse: Boolean = false,
    @SerialName("data_id")
    val dataId: String? = null
)

@Serializable
data class FileData(
    @SerialName("file_uri")
    val fileUri: String? = null,
    @SerialName("mime_type")
    val mimeType: String? = null,
    @Transient val localUri: String? = null
)

@Serializable
data class Part(
    @SerialName("file_data")
    val fileData: FileData? = null,
    val text: String? = null,
    @Transient val localId: String = UUID.randomUUID().toString()
) {
    constructor(fileData: FileData) : this(fileData, null)
}

@Serializable
data class Content(
    val parts: List<Part>? = null,
    val role: String? = null
)
