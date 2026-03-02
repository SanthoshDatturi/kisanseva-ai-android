package com.kisanseva.ai.domain.model.websocketModels

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
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
    @SerialName("request_id") val requestId: String? = null,
    val event: String? = null,
    @SerialName("workflow_id") val workflowId: String? = null,
    @SerialName("workflow_status") val workflowStatus: String? = null,
    val step: String? = null,
    val ts: String? = null
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

@Serializable
data class WorkflowWebSocketEvent(
    val action: String,
    val event: String,
    @SerialName("workflow_id") val workflowId: String? = null,
    @SerialName("workflow_status") val workflowStatus: String? = null,
    val step: String? = null,
    val data: JsonElement? = null,
    val ts: String? = null
)

@Serializable
data class WorkflowChunkEnvelope(
    @SerialName("chunk_type") val chunkType: String,
    val data: JsonElement? = null
)

object WorkflowEvents {
    const val WORKFLOW_STARTED = "workflow_started"
    const val STEP_STARTED = "step_started"
    const val STEP_COMPLETED = "step_completed"
    const val CHUNK = "chunk"
    const val RESULT = "result"
    const val WORKFLOW_COMPLETED = "workflow_completed"
    const val WORKFLOW_FAILED = "workflow_failed"
}
