package com.kisanseva.ai.ui.presentation.main.chat.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.data.remote.websocket.Actions
import com.kisanseva.ai.domain.model.ChatSession
import com.kisanseva.ai.domain.model.ChatType
import com.kisanseva.ai.domain.model.Content
import com.kisanseva.ai.domain.model.FileData
import com.kisanseva.ai.domain.model.FileType
import com.kisanseva.ai.domain.model.Message
import com.kisanseva.ai.domain.model.MessageRequest
import com.kisanseva.ai.domain.model.MessageState
import com.kisanseva.ai.domain.model.Part
import com.kisanseva.ai.domain.model.Role
import com.kisanseva.ai.domain.model.websocketModels.ChatWebSocketEvent
import com.kisanseva.ai.domain.model.websocketModels.Command
import com.kisanseva.ai.domain.repository.ChatRepository
import com.kisanseva.ai.domain.repository.FilesRepository
import com.kisanseva.ai.domain.state.Result
import com.kisanseva.ai.system.audio.player.AudioPlayer
import com.kisanseva.ai.system.storage.MediaStorageManager
import com.kisanseva.ai.ui.presentation.UiText
import com.kisanseva.ai.ui.presentation.asUiText
import com.kisanseva.ai.ui.presentation.main.chat.chat.ChatEvent.HandleCommand
import com.kisanseva.ai.util.UrlUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val chatSession: ChatSession? = null,
    val isRefreshing: Boolean = false,
    val chatType: ChatType = ChatType.GENERAL,
    val imageParts: List<Part> = emptyList(),
    val isRecording: Boolean = false,
    val audioFile: File? = null,
    val audioPart: Part? = null,
    val isUploading: Boolean = false,
    val command: Command? = null,
    val showBottomSheet: Boolean = false,
    val progressMessages: List<String> = emptyList()
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val filesRepository: FilesRepository,
    private val mediaStorageManager: MediaStorageManager,
    val audioPlayer: AudioPlayer,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    private val _chatEvent = MutableSharedFlow<ChatEvent>()
    val chatEvent = _chatEvent.asSharedFlow()

    private val _errorChannel = MutableSharedFlow<UiText>()
    val errorChannel = _errorChannel.asSharedFlow()

    var message by mutableStateOf("")
        private set

    private var chatId: String? = savedStateHandle.get<String>("chatId")
    private val chatType: ChatType = savedStateHandle.get<ChatType>("chatType") ?: ChatType.GENERAL
    private val dataId: String? = savedStateHandle.get<String>("dataId")


    init {
        _uiState.update { it.copy(chatType = chatType) }
        observeWorkflowProgress()
        
        viewModelScope.launch {
            if (chatId == null) {
                createNewChat()
            } else {
                observeChatSession(chatId!!)
                observeMessages(chatId!!)
                refreshMessages(chatId!!)
            }
        }
        observeEvents()
    }

    private fun observeWorkflowProgress() {
        val progressFlow = when (chatType) {
            ChatType.FARM_SURVEY -> chatRepository.listenToFarmSurveyProgress()
            ChatType.GENERAL -> chatRepository.listenToGeneralChatProgress()
            else -> chatRepository.listenToGeneralChatProgress()
        }

        progressFlow
            .onEach { message ->
                if (message.startsWith("Failed:", ignoreCase = true)) {
                    _errorChannel.emit(UiText.DynamicString(message.removePrefix("Failed:").trim()))
                    _uiState.update { it.copy(progressMessages = emptyList()) }
                    return@onEach
                }
                _uiState.update { current ->
                    val isAwaitingResponse =
                        current.chatSession?.lastUserMessageState?.state == MessageState.SENT
                    if (!isAwaitingResponse) return@update current
                    current.copy(
                        progressMessages = (current.progressMessages + message).distinct().takeLast(8)
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun createNewChat() {
        _uiState.update { it.copy(isRefreshing = true) }
        when (val result = chatRepository.createChatSession(chatType, dataId)) {
            is Result.Error -> {
                _errorChannel.emit(result.error.asUiText())
                _uiState.update { it.copy(isRefreshing = false) }
            }
            is Result.Success -> {
                chatId = result.data.id
                _uiState.update { it.copy(isRefreshing = false) }
                observeChatSession(chatId!!)
                observeMessages(chatId!!)
                refreshMessages(chatId!!)

                if (chatType == ChatType.FARM_SURVEY) {
                    message = "..."
                    sendMessage()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }

    private fun observeChatSession(chatId: String) {
        viewModelScope.launch {
            chatRepository.getChatSession(chatId).collectLatest { session ->
                _uiState.update { it.copy(chatSession = session) }
            }
        }
    }

    fun bottomSheetState(showBottomSheet: Boolean) {
        _uiState.update { it.copy(showBottomSheet = showBottomSheet) }
    }

    fun setCommand(command: Command?) {
        _uiState.update { it.copy(command = command) }
    }

    fun onMessageChange(newMessage: String) {
        message = newMessage
    }

    fun addImage(inputStream: InputStream, mimeType: String) {
        _uiState.update { it.copy(isUploading = true) }
        viewModelScope.launch {
            try {
                if (chatId.isNullOrBlank()) {
                    _errorChannel.emit(UiText.DynamicString("Upload failed: Chat ID not found"))
                    return@launch
                }
                val localFile = mediaStorageManager.saveImage(inputStream, mimeType = mimeType)
                val newPart = Part(
                    fileData = FileData(mimeType = mimeType, localUri = localFile.absolutePath),
                )
                _uiState.update { it.copy(imageParts = it.imageParts + newPart) }
                when (val result = filesRepository.uploadFile(
                    fileStream = localFile.inputStream(),
                    blobName = "${UUID.randomUUID()}",
                    fileType = FileType.AI_CHAT,
                    mimeType = mimeType,
                    pathPrefix = chatId!!
                )) {
                    is Result.Error -> {
                        _errorChannel.emit(result.error.asUiText())
                    }
                    is Result.Success -> {
                        _uiState.update { state ->
                            val updatedParts = state.imageParts.map {
                                if (it.localId == newPart.localId) {
                                    it.copy(fileData = it.fileData?.copy(fileUri = result.data.url))
                                } else {
                                    it
                                }
                            }
                            state.copy(imageParts = updatedParts)
                        }
                    }
                }
            } catch (e: Exception) {
                _errorChannel.emit(UiText.DynamicString(e.localizedMessage ?: "An unknown error occurred"))
            } finally {
                _uiState.update { it.copy(isUploading = false) }
            }
        }
    }

    fun removeImage(part: Part) {
        _uiState.update { chatState ->
            chatState.copy(imageParts = chatState.imageParts.filter { it.localId != part.localId })
        }
        part.fileData?.localUri?.let {
            mediaStorageManager.deleteFile(it)
        }
        viewModelScope.launch {
            part.fileData?.fileUri?.let {
                filesRepository.deleteFile(it, FileType.AI_CHAT)
            }
        }
    }

    fun onStartRecording(): File {
        audioPlayer.stop()
        return mediaStorageManager.createNewAudioFile()
    }

    fun onIsRecordingChange(isRecording: Boolean) {
        _uiState.update { it.copy(isRecording = isRecording) }
    }

    fun onAudioFileChange(audioFile: File?) {
        _uiState.update { it.copy(audioFile = audioFile) }
    }

    fun onRecordingComplete(audioFile: File?) {
        _uiState.update { it.copy(isUploading = true) }
        if (audioFile != null) {
            viewModelScope.launch {
                try {
                    if (chatId.isNullOrBlank()) {
                        _errorChannel.emit(UiText.DynamicString("Upload failed: Chat ID not found"))
                        return@launch
                    }
                    val newPart = Part(
                        fileData = FileData(
                            mimeType = "audio/mp4",
                            localUri = audioFile.absolutePath
                        ),
                    )
                    _uiState.update { it.copy(audioPart = newPart) }

                    when (val result = filesRepository.uploadFile(
                        fileStream = audioFile.inputStream(),
                        blobName = "${UUID.randomUUID()}",
                        fileType = FileType.AI_CHAT,
                        mimeType = "audio/mp4",
                        pathPrefix = chatId!!
                    )) {
                        is Result.Error -> {
                            _errorChannel.emit(result.error.asUiText())
                            mediaStorageManager.deleteFile(audioFile.absolutePath)
                            _uiState.update {
                                it.copy(audioPart = null)
                            }
                        }
                        is Result.Success -> {
                            _uiState.update { state ->
                                if (state.audioPart?.localId == newPart.localId) {
                                    state.copy(
                                        audioPart = state.audioPart.copy(
                                            fileData = state.audioPart.fileData?.copy(
                                                fileUri = result.data.url
                                            )
                                        )
                                    )
                                } else {
                                    state
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    mediaStorageManager.deleteFile(audioFile.absolutePath)
                    _errorChannel.emit(UiText.DynamicString("Audio upload failed"))
                    _uiState.update {
                        it.copy(audioPart = null)
                    }
                }
            }
        }
        _uiState.update { it.copy(isUploading = false, audioFile = audioFile) }
    }

    fun onRecordingCancel() {
        _uiState.value.audioFile?.let {
            mediaStorageManager.deleteFile(it.absolutePath)
        }
        viewModelScope.launch {
            _uiState.value.audioPart?.fileData?.fileUri?.let {
                filesRepository.deleteFile(it, FileType.AI_CHAT)
            }
        }
        _uiState.update { it.copy(audioFile = null, audioPart = null, isRecording = false) }
    }

    private fun observeMessages(chatId: String) {
        viewModelScope.launch {
            chatRepository.getChatMessages(chatId)
                .catch { e ->
                    _errorChannel.emit(UiText.DynamicString(e.localizedMessage ?: "An unknown error occurred"))
                }
                .collectLatest { messages ->
                    _uiState.update { it.copy(messages = messages) }
                }
        }
    }

    fun refreshMessages(chatId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            when (val result = chatRepository.refreshChatMessages(chatId)) {
                is Result.Error -> {
                    _errorChannel.emit(result.error.asUiText())
                }
                is Result.Success -> Unit
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun observeEvents() {
        chatRepository.observeWebSocketEvents()
            .onEach { event ->
                if (chatId == null || event.modelMessage.chatId != chatId) return@onEach

                event.modelMessage.let { modelMessage ->
                    if (_uiState.value.messages.none { it.id == modelMessage.id || (it.requestId != null && it.requestId == modelMessage.requestId && it.content.role == Role.MODEL.name.lowercase()) }) {
                        modelMessage.content.parts?.find {
                            it.fileData?.mimeType?.contains("audio") == true
                        }?.let { part ->
                            val url = (part.fileData?.fileUri ?: part.fileData?.localUri)?.let {
                                if (it.startsWith("http")) it else UrlUtils.getFullUrlFromRef(it)
                            }
                            url?.let { audioPlayer.play(it) }
                        }
                    }
                }
                when (event) {
                    is ChatWebSocketEvent.FarmSurveyEventChat -> {
                        _uiState.update { it.copy(progressMessages = emptyList()) }
                        _chatEvent.emit(HandleCommand(event.command, event.farmProfile))
                    }

                    is ChatWebSocketEvent.GeneralChatEventChat -> {
                        _uiState.update { it.copy(progressMessages = emptyList()) }
                        _chatEvent.emit(HandleCommand<Unit>(event.command))
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun sendMessage() {
        val currentChatId = chatId ?: return
        
        // Check if last message is resolved
        val lastState = uiState.value.chatSession?.lastUserMessageState?.state
        if (lastState != null && lastState != MessageState.RESOLVED) {
            return
        }

        val messageParts = mutableListOf<Part>()

        if (uiState.value.imageParts.isNotEmpty()) {
            messageParts.addAll(uiState.value.imageParts)
        }

        if (uiState.value.audioPart != null) {
            messageParts.add(uiState.value.audioPart!!)
        } else if (message.isNotBlank()) {
            messageParts.add(Part(text = message))
        } else return

        val userMessageContent = Content(
            parts = messageParts,
            role = Role.USER.name.lowercase()
        )

        val requestId = UUID.randomUUID().toString()

        val request = MessageRequest(
            chatId = currentChatId,
            content = userMessageContent,
            audioResponse = uiState.value.audioPart != null,
            requestId = requestId
        )

        val optimisticMessage = Message(
            chatId = currentChatId,
            content = userMessageContent,
            requestId = requestId
        )

        viewModelScope.launch {
            chatRepository.saveMessage(optimisticMessage)
            _uiState.update { it.copy(progressMessages = emptyList()) }
            
            val action = when (chatType) {
                ChatType.FARM_SURVEY -> Actions.FARM_SURVEY_AGENT
                ChatType.GENERAL -> Actions.GENERAL_CHAT
                else -> Actions.GENERAL_CHAT
            }
            
            when (val result = chatRepository.sendMessage(action = action, data = request)) {
                is Result.Error -> {
                    _errorChannel.emit(result.error.asUiText())
                    _uiState.update { it.copy(progressMessages = emptyList()) }
                }
                is Result.Success -> {
                    message = ""
                    _uiState.update {
                        it.copy(imageParts = emptyList(), audioPart = null, audioFile = null)
                    }
                }
            }
        }
    }
}
