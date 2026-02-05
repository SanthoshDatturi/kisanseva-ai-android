package com.kisanseva.ai.ui.presentation.main.chat.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.data.remote.websocket.Actions
import com.kisanseva.ai.domain.model.ChatType
import com.kisanseva.ai.domain.model.Content
import com.kisanseva.ai.domain.model.FileData
import com.kisanseva.ai.domain.model.FileType
import com.kisanseva.ai.domain.model.Message
import com.kisanseva.ai.domain.model.MessageRequest
import com.kisanseva.ai.domain.model.Part
import com.kisanseva.ai.domain.model.Role
import com.kisanseva.ai.domain.model.websocketModels.ChatWebSocketEvent
import com.kisanseva.ai.domain.model.websocketModels.Command
import com.kisanseva.ai.domain.repository.ChatRepository
import com.kisanseva.ai.domain.repository.FilesRepository
import com.kisanseva.ai.exception.ApiException
import com.kisanseva.ai.system.audio.player.AudioPlayer
import com.kisanseva.ai.system.storage.MediaStorageManager
import com.kisanseva.ai.ui.presentation.main.chat.chat.ChatEvent.HandleCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val isLoading: Boolean = false,
    val isSendingMessage: Boolean = false,
    val error: String? = null,
    val chatType: ChatType = ChatType.GENERAL,
    val imageParts: List<Part> = emptyList(),
    val isRecording: Boolean = false,
    val audioFile: File? = null,
    val audioPart: Part? = null,
    val isUploading: Boolean = false,
    val command: Command? = null,
    val showBottomSheet: Boolean = false
)

const val TEMP_ID_PREFIX = "temp-"
const val PERM_CHAT_ID_SEPARATOR = "-perm-"

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

    var message by mutableStateOf("")
        private set

    private var chatId: String? = savedStateHandle.get<String>("chatId")
    private val chatType: ChatType? = savedStateHandle.get<ChatType>("chatType")
    private val dataId: String? = savedStateHandle.get<String>("dataId")


    init {
        _uiState.update { it.copy(chatType = chatType!!) }
        chatId?.let { chatId ->
            if (!chatId.startsWith(TEMP_ID_PREFIX)) {
                loadMessages(chatId)
            } else {
                _uiState.update { it.copy(messages = emptyList()) }
                if (chatType == ChatType.FARM_SURVEY) {
                    message = "..."
                    sendMessage()
                }
            }
        }
        observeEvents()
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
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
                val localFile = mediaStorageManager.saveImage(inputStream, mimeType = mimeType)
                val newPart = Part(
                    fileData = FileData(mimeType = mimeType, localUri = localFile.absolutePath),
                )
                _uiState.update { it.copy(imageParts = it.imageParts + newPart) }

                val response = filesRepository.uploadFile(
                    fileStream = localFile.inputStream(),
                    blobName = "${UUID.randomUUID()}",
                    fileType = FileType.IMAGE,
                    mimeType = mimeType
                )
                _uiState.update { state ->
                    val updatedParts = state.imageParts.map {
                        if (it.localId == newPart.localId) {
                            it.copy(fileData = it.fileData?.copy(fileUri = response.url))
                        } else {
                            it
                        }
                    }
                    state.copy(imageParts = updatedParts)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage ?: "An unknown error occurred") }
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
                filesRepository.deleteFile(it, FileType.IMAGE)
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
                    val newPart = Part(
                        fileData = FileData(
                            mimeType = "audio/mp4",
                            localUri = audioFile.absolutePath
                        ),
                    )
                    _uiState.update { it.copy(audioPart = newPart) }

                    val response = filesRepository.uploadFile(
                        fileStream = audioFile.inputStream(),
                        blobName = "${UUID.randomUUID()}",
                        fileType = FileType.AUDIO,
                        mimeType = "audio/mp4"
                    )

                    _uiState.update { state ->
                        if (state.audioPart?.localId == newPart.localId) {
                            state.copy(
                                audioPart = state.audioPart.copy(
                                    fileData = state.audioPart.fileData?.copy(
                                        fileUri = response.url
                                    )
                                )
                            )
                        } else {
                            state
                        }
                    }
                } catch (_: Exception) {
                    mediaStorageManager.deleteFile(audioFile.absolutePath)
                    _uiState.update {
                        it.copy(
                            error = "Audio upload failed",
                            audioPart = null
                        )
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
                filesRepository.deleteFile(it, FileType.AUDIO)
            }
        }
        _uiState.update { it.copy(audioFile = null, audioPart = null, isRecording = false) }
    }

    private fun loadMessages(chatId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val messages = chatRepository.getChatMessages(chatId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        messages = messages
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "An unknown error occurred"
                    )
                }
            }
        }
    }

    private fun observeEvents() {
        chatRepository.observeWebSocketEvents()
            .onEach { event ->
                if (chatId == null || !event.modelMessage.chatId.contains(chatId!!)) return@onEach

                _uiState.update { it.copy(isSendingMessage = false) }
                event.modelMessage.let { modelMessage ->

                    if (modelMessage.chatId.startsWith(TEMP_ID_PREFIX)) {
                        val newChatId = modelMessage.chatId.split(PERM_CHAT_ID_SEPARATOR).last()
                        val tempChatId = modelMessage.chatId.split(PERM_CHAT_ID_SEPARATOR).first()

                        this.chatId = newChatId

                        _uiState.update { currentState ->
                            val updatedMessages = currentState.messages.map {
                                if (it.chatId == tempChatId) {
                                    it.copy(chatId = newChatId)
                                } else {
                                    it
                                }
                            }
                            currentState.copy(messages = updatedMessages)
                        }
                        modelMessage.chatId = newChatId
                    }

                    if (_uiState.value.messages.none { it.id == modelMessage.id }) {
                        modelMessage.content.parts?.find {
                            it.fileData?.mimeType?.contains("audio") == true
                        }?.let { part ->
                            (part.fileData?.localUri ?: part.fileData?.fileUri)?.let {
                                audioPlayer.play(it)
                            }
                        }
                    }

                    _uiState.update { currentState ->
                        if (currentState.messages.any { it.id == modelMessage.id }) {
                            currentState
                        } else {
                            currentState.copy(
                                messages = currentState.messages + modelMessage
                            )
                        }
                    }
                }
                event.userMessage?.let { userMessage ->
                    _uiState.update { currentState ->
                        val updatedMessages = currentState.messages.map {
                            if (it.id.startsWith(TEMP_ID_PREFIX)) {
                                chatRepository.deleteMessage(it.id)
                                it.copy(id = userMessage.id)
                            }
                            it.copy(ts = userMessage.ts)
                            chatRepository.saveMessage(it)
                        }
                        currentState.copy(messages = updatedMessages)
                    }
                }
                when (event) {
                    is ChatWebSocketEvent.FarmSurveyEventChat -> {
                        _chatEvent.emit(HandleCommand(event.command, event.farmProfile))
                    }

                    is ChatWebSocketEvent.GeneralChatEventChat -> {
                        _chatEvent.emit(HandleCommand<Unit>(event.command))
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun sendMessage() {
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

        val request = MessageRequest(
            chatId = chatId,
            content = userMessageContent,
            audioResponse = uiState.value.audioPart != null
        )

        val optimisticMessage = Message(
            id = "${TEMP_ID_PREFIX}${UUID.randomUUID()}",
            chatId = chatId ?: "",
            content = userMessageContent,
        )

        _uiState.update {
            it.copy(
                messages = it.messages + optimisticMessage,
                isSendingMessage = true
            )
        }

        viewModelScope.launch {
            chatRepository.saveMessage(optimisticMessage)
        }

        try {
            val action = when (chatType ?: ChatType.GENERAL.name) {
                ChatType.FARM_SURVEY -> Actions.FARM_SURVEY_AGENT
                ChatType.GENERAL -> Actions.GENERAL_CHAT
                else -> {
                    throw ApiException(
                        404,
                        message = "Chat type not found"
                    )
                }
            }
            viewModelScope.launch {
                chatRepository.sendMessage(
                    action = action,
                    data = request
                )
            }
            message = ""
            _uiState.update {
                it.copy(imageParts = emptyList(), audioPart = null, audioFile = null)
            }
        } catch (e: ApiException) {
            _uiState.update {
                it.copy(
                    error = e.message,
                    isSendingMessage = false
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    error = e.localizedMessage ?: "An unknown error occurred",
                    isSendingMessage = false
                )
            }
        }
    }
}
