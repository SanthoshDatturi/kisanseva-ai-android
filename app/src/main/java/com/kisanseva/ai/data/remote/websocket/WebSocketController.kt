package com.kisanseva.ai.data.remote.websocket

import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kisanseva.ai.BuildConfig
import com.kisanseva.ai.data.local.DataStoreManager
import com.kisanseva.ai.data.local.dao.QueuedMessageDao
import com.kisanseva.ai.data.local.entity.QueuedMessageEntity
import com.kisanseva.ai.data.remote.toNetworkError
import com.kisanseva.ai.di.AuthenticatedClient
import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.CropRecommendationResponse
import com.kisanseva.ai.domain.model.PesticideRecommendationError
import com.kisanseva.ai.domain.model.PesticideRecommendationResponse
import com.kisanseva.ai.domain.model.websocketModels.BaseWebSocketRequest
import com.kisanseva.ai.domain.model.websocketModels.BaseWebSocketResponse
import com.kisanseva.ai.domain.model.websocketModels.CropSelectionResponse
import com.kisanseva.ai.domain.model.websocketModels.FarmSurveyAgentResponse
import com.kisanseva.ai.domain.model.websocketModels.GeneralChatResponse
import com.kisanseva.ai.domain.model.websocketModels.TextToSpeechUrlResponseData
import com.kisanseva.ai.domain.model.websocketModels.WebSocketError
import com.kisanseva.ai.domain.model.websocketModels.WorkflowEvents
import com.kisanseva.ai.domain.model.websocketModels.WorkflowWebSocketEvent
import com.kisanseva.ai.util.ConnectivityObserver
import com.kisanseva.ai.workers.MessageQueueWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.pow


object Actions {
    const val FARM_SURVEY_AGENT = "farm_survey_agent"
    const val CROP_RECOMMENDATION = "crop_recommendation"
    const val SELECT_CROP_FROM_RECOMMENDATION = "select_crop_from_recommendation"
    const val PESTICIDE_RECOMMENDATION = "pesticide_recommendation"
    const val TEXT_TO_SPEECH_URL = "text_to_speech_url"
    const val GENERAL_CHAT = "general_chat"
}

@Serializable
data class RawBaseWebSocketResponse(
    val action: String,
    val data: JsonElement? = null,
    val error: WebSocketError? = null,
    val event: String? = null,
    @SerialName("workflow_id") val workflowId: String? = null,
    @SerialName("workflow_status") val workflowStatus: String? = null,
    val step: String? = null,
    val ts: String? = null
)

enum class ConnectionState {
    CONNECTED, CONNECTING, DISCONNECTED, ERROR
}

@Singleton
class WebSocketController @Inject constructor(
    @AuthenticatedClient private val okHttpClient: OkHttpClient,
    private val dataStoreManager: DataStoreManager,
    private val connectivityObserver: ConnectivityObserver,
    private val queuedMessageDao: QueuedMessageDao,
    private val workManager: WorkManager,
    val json: Json
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var webSocket: WebSocket? = null
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _messages = MutableSharedFlow<BaseWebSocketResponse<*>>(extraBufferCapacity = 100)
    val messages: Flow<BaseWebSocketResponse<*>> = _messages

    private val _workflowEvents = MutableSharedFlow<WorkflowWebSocketEvent>(extraBufferCapacity = 200)
    val workflowEvents: Flow<WorkflowWebSocketEvent> = _workflowEvents

    private val _errors = MutableSharedFlow<DataError.Network>(extraBufferCapacity = 10)
    val errors: Flow<DataError.Network> = _errors

    private var reconnectAttempt = 0

    init {
        scope.launch {
            connectivityObserver.observe().collect { status ->
                when (status) {
                    ConnectivityObserver.Status.Available -> {
                        Log.d(TAG, "Network available, connecting...")
                        resetReconnect()
                        connect()
                    }
                    else -> {
                        Log.d(TAG, "Network unavailable, disconnecting...")
                        disconnect()
                    }
                }
            }
        }
    }
    
    fun isConnected(): Boolean = _connectionState.value == ConnectionState.CONNECTED

    private fun getListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                this@WebSocketController.webSocket = webSocket
                _connectionState.value = ConnectionState.CONNECTED
                reconnectAttempt = 0
                Log.d(TAG, "WebSocket Connected")
                scope.launch { flushQueue() }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received: $text")
                scope.launch {
                    try {
                        val rawResponse = json.decodeFromString<RawBaseWebSocketResponse>(text)

                        if (rawResponse.error != null) {
                            _errors.emit(rawResponse.error.statusCode.toNetworkError())
                            return@launch
                        }

                        if (!rawResponse.event.isNullOrBlank()) {
                            _workflowEvents.emit(
                                WorkflowWebSocketEvent(
                                    action = rawResponse.action,
                                    event = rawResponse.event,
                                    workflowId = rawResponse.workflowId,
                                    workflowStatus = rawResponse.workflowStatus,
                                    step = rawResponse.step,
                                    data = rawResponse.data,
                                    ts = rawResponse.ts
                                )
                            )

                            if (rawResponse.event != WorkflowEvents.RESULT) {
                                return@launch
                            }
                        }

                        val finalResponse = rawResponse.data?.let {
                            when (rawResponse.action) {
                                Actions.FARM_SURVEY_AGENT -> BaseWebSocketResponse(
                                    rawResponse.action,
                                    json.decodeFromJsonElement<FarmSurveyAgentResponse>(it),
                                    event = rawResponse.event,
                                    workflowId = rawResponse.workflowId,
                                    workflowStatus = rawResponse.workflowStatus,
                                    step = rawResponse.step,
                                    ts = rawResponse.ts
                                )
                                Actions.CROP_RECOMMENDATION -> BaseWebSocketResponse(
                                    rawResponse.action,
                                    json.decodeFromJsonElement<CropRecommendationResponse>(it),
                                    event = rawResponse.event,
                                    workflowId = rawResponse.workflowId,
                                    workflowStatus = rawResponse.workflowStatus,
                                    step = rawResponse.step,
                                    ts = rawResponse.ts
                                )
                                Actions.SELECT_CROP_FROM_RECOMMENDATION -> BaseWebSocketResponse(
                                    rawResponse.action,
                                    json.decodeFromJsonElement<CropSelectionResponse>(
                                        it
                                    ),
                                    event = rawResponse.event,
                                    workflowId = rawResponse.workflowId,
                                    workflowStatus = rawResponse.workflowStatus,
                                    step = rawResponse.step,
                                    ts = rawResponse.ts
                                )
                                Actions.PESTICIDE_RECOMMENDATION -> BaseWebSocketResponse(
                                    action = rawResponse.action,
                                    data = runCatching {
                                        val jsonObject = it.jsonObject
                                        if ("recommendations" in jsonObject) {
                                            json.decodeFromJsonElement<PesticideRecommendationResponse>(it)
                                        } else {
                                            val errorPayload = json.decodeFromJsonElement<PesticideRecommendationError>(it)
                                            Log.w(TAG, "Pesticide recommendation failed: ${errorPayload.reason}")
                                            null
                                        }
                                    }.getOrNull(),
                                    event = rawResponse.event,
                                    workflowId = rawResponse.workflowId,
                                    workflowStatus = rawResponse.workflowStatus,
                                    step = rawResponse.step,
                                    ts = rawResponse.ts
                                )
                                Actions.TEXT_TO_SPEECH_URL -> BaseWebSocketResponse(
                                    rawResponse.action,
                                    json.decodeFromJsonElement<TextToSpeechUrlResponseData>(it),
                                    event = rawResponse.event,
                                    workflowId = rawResponse.workflowId,
                                    workflowStatus = rawResponse.workflowStatus,
                                    step = rawResponse.step,
                                    ts = rawResponse.ts
                                )
                                Actions.GENERAL_CHAT -> BaseWebSocketResponse(
                                    rawResponse.action,
                                    json.decodeFromJsonElement<GeneralChatResponse>(it),
                                    event = rawResponse.event,
                                    workflowId = rawResponse.workflowId,
                                    workflowStatus = rawResponse.workflowStatus,
                                    step = rawResponse.step,
                                    ts = rawResponse.ts
                                )
                                else -> BaseWebSocketResponse(
                                    action = rawResponse.action,
                                    data = Unit,
                                    event = rawResponse.event,
                                    workflowId = rawResponse.workflowId,
                                    workflowStatus = rawResponse.workflowStatus,
                                    step = rawResponse.step,
                                    ts = rawResponse.ts
                                )
                            }
                        } ?: BaseWebSocketResponse(
                            action = rawResponse.action,
                            data = Unit,
                            event = rawResponse.event,
                            workflowId = rawResponse.workflowId,
                            workflowStatus = rawResponse.workflowStatus,
                            step = rawResponse.step,
                            ts = rawResponse.ts
                        )
                        if (finalResponse.data != null || rawResponse.action == Actions.TEXT_TO_SPEECH_URL) {
                            _messages.emit(finalResponse)
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing message", e)
                        _errors.emit(DataError.Network.SERIALIZATION)
                    }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Closing: $code / $reason")
                _connectionState.value = ConnectionState.DISCONNECTED
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                this@WebSocketController.webSocket = null
                _connectionState.value = ConnectionState.DISCONNECTED
                Log.d(TAG, "Closed: $code / $reason")
                if (code != NORMAL_CLOSURE_STATUS) {
                    reconnect()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                this@WebSocketController.webSocket = null
                _connectionState.value = ConnectionState.ERROR
                Log.e(TAG, "Error: ${t.message}", t)
                scope.launch {
                    _errors.emit(response?.code?.toNetworkError() ?: DataError.Network.UNKNOWN)
                }
                reconnect()
            }
        }
    }

    fun connect() {
        if (_connectionState.value == ConnectionState.CONNECTED || _connectionState.value == ConnectionState.CONNECTING) return
        
        _connectionState.value = ConnectionState.CONNECTING
        scope.launch {
            val token = dataStoreManager.token.first()
            if (token == null) {
                _connectionState.value = ConnectionState.ERROR
                _errors.emit(DataError.Network.UNAUTHORIZED)
                return@launch
            }
            val request = Request.Builder()
                .url(WEB_SOCKET_URL)
                .build()
            okHttpClient.newWebSocket(request, getListener())
        }
    }

    private fun reconnect() {
        if (_connectionState.value == ConnectionState.CONNECTING) return
        
        scope.launch {
            val delayMs = calculateReconnectDelay()
            Log.d(TAG, "Reconnecting in $delayMs ms (Attempt ${reconnectAttempt + 1})")
            delay(delayMs)
            reconnectAttempt++
            connect()
        }
    }

    private fun calculateReconnectDelay(): Long {
        val baseDelay = 1000L
        val maxDelay = 30000L
        val delay = baseDelay * (2.0.pow(reconnectAttempt.toDouble())).toLong()
        return min(delay, maxDelay)
    }

    private fun resetReconnect() {
        reconnectAttempt = 0
    }


    inline fun <reified T : Any> sendMessage(action: String, data: T, queueIfOffline: Boolean = true) {
        val request = BaseWebSocketRequest(action, data)
        val jsonMessage = json.encodeToString(request)
        if (!sendJsonMessage(jsonMessage) && queueIfOffline) {
            enqueueMessage(action, jsonMessage)
        }
    }

    fun enqueueMessage(action: String, jsonMessage: String) {
        scope.launch {
            queuedMessageDao.insertMessage(QueuedMessageEntity(action = action, messageJson = jsonMessage))
            triggerWorker()
        }
    }

    private fun triggerWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<MessageQueueWorker>()
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniqueWork(
            "MessageQueueWorker_OneTime",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun sendJsonMessage(jsonMessage: String): Boolean {
        val ws = webSocket
        return if (ws != null && _connectionState.value == ConnectionState.CONNECTED) {
            Log.d(TAG, "Sending: $jsonMessage")
            ws.send(jsonMessage)
        } else {
            Log.w(TAG, "Failed to send message: WebSocket not connected")
            false
        }
    }

    suspend fun flushQueue(): Boolean {
        val queuedMessages = queuedMessageDao.getQueuedMessages().first()
        if (queuedMessages.isEmpty()) return true
        
        Log.d(TAG, "Flushing ${queuedMessages.size} messages")
        var allSuccess = true
        for (message in queuedMessages) {
            if (sendJsonMessage(message.messageJson)) {
                queuedMessageDao.deleteMessage(message.id)
            } else {
                Log.w(TAG, "Failed to send queued message ${message.id}, stopping flush")
                allSuccess = false
                break
            }
        }
        return allSuccess
    }

    fun disconnect() {
        webSocket?.close(NORMAL_CLOSURE_STATUS, "User disconnected")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    companion object {
        const val TAG = "WebSocketController"
        private const val WEB_SOCKET_URL = "wss://${BuildConfig.BASE_URL}/ws"
        private const val NORMAL_CLOSURE_STATUS = 1000
    }
}
