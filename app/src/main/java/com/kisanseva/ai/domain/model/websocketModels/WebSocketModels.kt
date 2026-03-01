package com.kisanseva.ai.domain.model.websocketModels

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class BaseWebSocketRequest<T>(
    val action: String,
    val data: T,
    val id: String = UUID.randomUUID().toString()
)

@Serializable
data class BaseWebSocketResponse<T>(
    val action: String,
    val data: T? = null,
    val error: WebSocketError? = null,
    @SerialName("request_id") val requestId: String? = null
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
