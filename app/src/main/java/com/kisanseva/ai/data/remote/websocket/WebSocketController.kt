package com.kisanseva.ai.data.remote.websocket

import android.util.Log
import com.kisanseva.ai.BuildConfig
import com.kisanseva.ai.data.local.DataStoreManager
import com.kisanseva.ai.data.remote.toNetworkError
import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.CropRecommendationResponse
import com.kisanseva.ai.domain.model.PesticideRecommendationResponse
import com.kisanseva.ai.domain.model.websocketModels.BaseWebSocketRequest
import com.kisanseva.ai.domain.model.websocketModels.BaseWebSocketResponse
import com.kisanseva.ai.domain.model.websocketModels.CropSelectionResponse
import com.kisanseva.ai.domain.model.websocketModels.FarmSurveyAgentResponse
import com.kisanseva.ai.domain.model.websocketModels.GeneralChatResponse
import com.kisanseva.ai.domain.model.websocketModels.TextToSpeechUrlResponseData
import com.kisanseva.ai.domain.model.websocketModels.WebSocketError
import com.kisanseva.ai.util.ConnectivityObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton


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
    val error: WebSocketError? = null
)


@Singleton
class WebSocketController @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val dataStoreManager: DataStoreManager,
    private val connectivityObserver: ConnectivityObserver,
    val json: Json
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var webSocket: WebSocket? = null
    private var isConnecting = false

    private val _messages = MutableSharedFlow<BaseWebSocketResponse<*>>(replay = 1)
    val messages: Flow<BaseWebSocketResponse<*>> = _messages

    private val _errors = MutableSharedFlow<DataError.Network>()
    val errors: Flow<DataError.Network> = _errors

    init {
        scope.launch {
            connectivityObserver.observe().collect { status ->
                when (status) {
                    ConnectivityObserver.Status.Available -> {
                        Log.d(TAG, "Network available, connecting...")
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
    
    fun isConnected(): Boolean = webSocket != null

    private fun getListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                this@WebSocketController.webSocket = webSocket
                isConnecting = false
                Log.d(TAG, "WebSocket Connected")
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

                        val finalResponse = rawResponse.data?.let {
                            when (rawResponse.action) {
                                Actions.FARM_SURVEY_AGENT -> BaseWebSocketResponse(
                                    rawResponse.action,
                                    json.decodeFromJsonElement<FarmSurveyAgentResponse>(it)
                                )
                                Actions.CROP_RECOMMENDATION -> BaseWebSocketResponse(
                                    rawResponse.action,
                                    json.decodeFromJsonElement<CropRecommendationResponse>(it)
                                )
                                Actions.SELECT_CROP_FROM_RECOMMENDATION -> BaseWebSocketResponse(
                                    rawResponse.action,
                                    json.decodeFromJsonElement<CropSelectionResponse>(
                                        it
                                    )
                                )
                                Actions.PESTICIDE_RECOMMENDATION -> BaseWebSocketResponse(
                                    rawResponse.action,
                                    json.decodeFromJsonElement<PesticideRecommendationResponse>(
                                        it
                                    )
                                )
                                Actions.TEXT_TO_SPEECH_URL -> BaseWebSocketResponse(
                                    rawResponse.action,
                                    json.decodeFromJsonElement<TextToSpeechUrlResponseData>(it)
                                )
                                Actions.GENERAL_CHAT -> BaseWebSocketResponse(
                                    rawResponse.action,
                                    json.decodeFromJsonElement<GeneralChatResponse>(it)
                                )
                                else -> BaseWebSocketResponse(rawResponse.action, Unit)
                            }
                        } ?: BaseWebSocketResponse(rawResponse.action, Unit)
                        _messages.emit(finalResponse)

                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing message", e)
                        _errors.emit(DataError.Network.SERIALIZATION)
                    }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                Log.d(TAG, "Closing: $code / $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                this@WebSocketController.webSocket = null
                isConnecting = false
                Log.d(TAG, "Closed: $code / $reason")
                if (code != NORMAL_CLOSURE_STATUS) {
                    reconnect()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                this@WebSocketController.webSocket = null
                isConnecting = false
                Log.e(TAG, "Error: ${t.message}", t)
                scope.launch {
                    _errors.emit(response?.code?.toNetworkError() ?: DataError.Network.UNKNOWN)
                }
                reconnect()
            }
        }
    }

    fun connect() {
        if (webSocket != null || isConnecting) return
        isConnecting = true
        scope.launch {
            val token = dataStoreManager.token.first()
            if (token == null) {
                isConnecting = false
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
        scope.launch {
            delay(RECONNECT_DELAY)
            Log.d(TAG, "Reconnecting...")
            connect()
        }
    }


    inline fun <reified T> sendMessage(action: String, data: T) {
        val request = BaseWebSocketRequest(action, data)
        val jsonMessage = json.encodeToString(request)
        sendJsonMessage(jsonMessage)
    }

    fun sendJsonMessage(jsonMessage: String) {
        if (webSocket == null) {
            scope.launch {
                _errors.emit(DataError.Network.NO_INTERNET)
            }
            return
        }
        Log.d(TAG, "Sending: $jsonMessage")
        webSocket?.send(jsonMessage)
    }

    fun disconnect() {
        webSocket?.close(NORMAL_CLOSURE_STATUS, "User disconnected")
        webSocket = null
        isConnecting = false
    }

    companion object {
        const val TAG = "WebSocketController"
        private const val WEB_SOCKET_URL = "wss://${BuildConfig.BASE_URL}/ws"
        private const val NORMAL_CLOSURE_STATUS = 1000
        private const val RECONNECT_DELAY = 5000L
    }
}
