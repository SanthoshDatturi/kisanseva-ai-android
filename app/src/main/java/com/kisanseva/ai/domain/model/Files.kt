package com.kisanseva.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FileUploadResponse(
    val url: String
)

@Serializable
data class TextToSpeechRequest(
    val text: String,
    @SerialName("blob_name")
    val blobName: String,
    val modulation: VoiceModulation = VoiceModulation.GENERAL,
    @SerialName("voice_name")
    val voiceName: VoiceName = VoiceName.KORE
)

@Serializable
data class FileDeleteRequest(
    val url: String,
    @SerialName("file_type")
    val fileType: FileType
)

@Serializable
enum class FileType(val value: String) {
    @SerialName("user-content")
    USER_CONTENT("user-content"),
    @SerialName("ai-chat")
    AI_CHAT("ai-chat"),
    @SerialName("system-data")
    SYSTEM_DATA("system-data")
}

@Serializable
enum class FileFolder(val value: String) {
    @SerialName("images")
    IMAGES("images"),
    @SerialName("audio")
    AUDIO("audio"),
    @SerialName("documents")
    DOCUMENTS("documents"),
    @SerialName("crops")
    CROPS("crops")
}

@Serializable
enum class VoiceModulation {
    @SerialName("general")
    GENERAL,
    @SerialName("data_explanation")
    DATA_EXPLANATION
}

@Serializable
enum class VoiceName {
    @SerialName("Kore")
    KORE
}
