package com.kisanseva.ai.domain.model.websocketModels

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseWebSocketRequest<T>(
    val action: String,
    val data: T
)

@Serializable
data class BaseWebSocketResponse<T>(
    val action: String,
    val data: T? = null,
    val error: WebSocketError? = null
)

@Serializable
data class WebSocketError(
    @SerialName("status_code") val statusCode: Int,
    val message: String
)

@Serializable
enum class Command {
    @SerialName("continue") CONTINUE,
    @SerialName("exit") EXIT,
    @SerialName("open_camera") OPEN_CAMERA,
    @SerialName("location") LOCATION
}

@Serializable
data class TextToSpeechUrlResponseData(
    val url: String
)
