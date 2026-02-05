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
enum class FileType {
    @SerialName("image")
    IMAGE,
    @SerialName("audio")
    AUDIO,
    @SerialName("video")
    VIDEO,
    @SerialName("document")
    DOCUMENT
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
