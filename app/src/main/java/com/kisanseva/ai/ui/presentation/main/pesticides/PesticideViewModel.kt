package com.kisanseva.ai.ui.presentation.main.pesticides

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kisanseva.ai.domain.model.FileData
import com.kisanseva.ai.domain.model.FileType
import com.kisanseva.ai.domain.model.Part
import com.kisanseva.ai.domain.model.PesticideInfo
import com.kisanseva.ai.domain.model.PesticideStage
import com.kisanseva.ai.domain.repository.FilesRepository
import com.kisanseva.ai.domain.repository.PesticideRecommendationRepository
import com.kisanseva.ai.domain.state.Result
import com.kisanseva.ai.system.audio.player.AudioPlayer
import com.kisanseva.ai.system.storage.MediaStorageManager
import com.kisanseva.ai.ui.navigation.PesticideDest
import com.kisanseva.ai.ui.presentation.UiText
import com.kisanseva.ai.ui.presentation.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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

data class PesticideUiState(
    val isRefreshing: Boolean = false,
    val cropId: String? = null,
    val farmId: String? = null,
    val previousPesticides: List<Pair<String, PesticideInfo>> = emptyList(),
    val isUploading: Boolean = false,
    val imageParts: List<Part> = emptyList(),
    val isRecording: Boolean = false,
    val audioFile: File? = null,
    val audioPart: Part? = null,
    val showDescriptionInput: Boolean = false,
    val isRequesting: Boolean = false
)

@HiltViewModel
class PesticideViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val pesticideRepository: PesticideRecommendationRepository,
    private val filesRepository: FilesRepository,
    private val mediaStorageManager: MediaStorageManager,
    val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(PesticideUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PesticideEvent>()
    val events = _events.asSharedFlow()

    private val _errorChannel = MutableSharedFlow<UiText>()
    val errorChannel = _errorChannel.asSharedFlow()

    var description by mutableStateOf("")
        private set

    private var pesticidesJob: Job? = null

    init {
        val navArgs = savedStateHandle.toRoute<PesticideDest.PesticideList>()
        val cropId = navArgs.cropId
        val farmId = navArgs.farmId
        _uiState.update { it.copy(cropId = cropId, farmId = farmId) }
        observeRecommendations(cropId)
        observePreviousPesticides(cropId)
        refreshPreviousPesticides(cropId)
    }

    private fun observeRecommendations(cropId: String) {
        pesticideRepository.listenToPesticideRecommendations()
            .onEach { recommendation ->
                if (recommendation.cropId == cropId) {
                    _events.emit(PesticideEvent.RecommendationReceived(recommendation.id))
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observePreviousPesticides(cropId: String) {
        pesticidesJob?.cancel()
        pesticidesJob = viewModelScope.launch {
            pesticideRepository.getRecommendationsByCropId(cropId)
                .catch { e ->
                    _errorChannel.emit(UiText.DynamicString(e.localizedMessage ?: "An error occurred"))
                }
                .collectLatest { recommendations ->
                    val pesticideItems = recommendations.flatMap { rec ->
                        rec.recommendations
                            .filter { it.stage != PesticideStage.RECOMMENDED }
                            .map { rec.id to it }
                    }
                    _uiState.update { it.copy(previousPesticides = pesticideItems) }
                }
        }
    }

    fun refreshPreviousPesticides(cropId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            when (val result = pesticideRepository.refreshRecommendationsByCropId(cropId)) {
                is Result.Error -> _errorChannel.emit(result.error.asUiText())
                is Result.Success -> Unit
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun onDescriptionChange(newDescription: String) {
        description = newDescription
    }

    fun addImage(inputStream: InputStream, mimeType: String) {
        _uiState.update { it.copy(isUploading = true) }
        viewModelScope.launch {
            try {
                val cropId = _uiState.value.cropId
                if (cropId.isNullOrBlank()) {
                    _errorChannel.emit(UiText.DynamicString("Upload failed: Crop ID not found"))
                    return@launch
                }
                val localFile = mediaStorageManager.saveImage(inputStream, mimeType = mimeType)
                val newPart = Part(
                    fileData = FileData(mimeType = mimeType, localUri = localFile.absolutePath),
                )
                _uiState.update { it.copy(imageParts = it.imageParts + newPart) }

                when (val result = filesRepository.uploadFile(
                    fileStream = localFile.inputStream(),
                    blobName = UUID.randomUUID().toString(),
                    fileType = FileType.USER_CONTENT,
                    mimeType = mimeType,
                    pathPrefix = cropId
                )) {
                    is Result.Error -> _errorChannel.emit(result.error.asUiText())
                    is Result.Success -> {
                        _uiState.update { state ->
                            val updatedParts = state.imageParts.map {
                                if (it.localId == newPart.localId) {
                                    it.copy(fileData = it.fileData?.copy(fileUri = result.data.url))
                                } else it
                            }
                            state.copy(imageParts = updatedParts, showDescriptionInput = true)
                        }
                    }
                }
            } catch (e: Exception) {
                _errorChannel.emit(UiText.DynamicString(e.localizedMessage ?: "An error occurred"))
            } finally {
                _uiState.update { it.copy(isUploading = false) }
            }
        }
    }

    fun removeImage(part: Part) {
        _uiState.update { state ->
            val newParts = state.imageParts.filter { it.localId != part.localId }
            state.copy(imageParts = newParts, showDescriptionInput = newParts.isNotEmpty() || state.audioPart != null)
        }
        part.fileData?.localUri?.let { mediaStorageManager.deleteFile(it) }
        viewModelScope.launch {
            part.fileData?.fileUri?.let { filesRepository.deleteFile(it, FileType.USER_CONTENT) }
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
        if (audioFile == null) return
        _uiState.update { it.copy(isUploading = true) }
        viewModelScope.launch {
            try {
                val cropId = _uiState.value.cropId
                if (cropId.isNullOrBlank()) {
                    _errorChannel.emit(UiText.DynamicString("Upload failed: Crop ID not found"))
                    return@launch
                }
                val newPart = Part(
                    fileData = FileData(mimeType = "audio/mp4", localUri = audioFile.absolutePath),
                )
                _uiState.update { it.copy(audioPart = newPart) }

                when (val result = filesRepository.uploadFile(
                    fileStream = audioFile.inputStream(),
                    blobName = UUID.randomUUID().toString(),
                    fileType = FileType.USER_CONTENT,
                    mimeType = "audio/mp4",
                    pathPrefix = cropId
                )) {
                    is Result.Error -> {
                        _errorChannel.emit(result.error.asUiText())
                        _uiState.update { it.copy(audioPart = null) }
                    }
                    is Result.Success -> {
                        _uiState.update { state ->
                            if (state.audioPart?.localId == newPart.localId) {
                                state.copy(
                                    audioPart = state.audioPart.copy(fileData = state.audioPart.fileData?.copy(fileUri = result.data.url)),
                                    showDescriptionInput = true
                                )
                            } else state
                        }
                    }
                }
            } catch (e: Exception) {
                mediaStorageManager.deleteFile(audioFile.absolutePath)
                _errorChannel.emit(UiText.DynamicString(e.localizedMessage ?: "Audio upload failed"))
                _uiState.update { it.copy(audioPart = null) }
            } finally {
                _uiState.update { it.copy(isUploading = false) }
            }
        }
    }

    fun onRecordingCancel() {
        _uiState.value.audioFile?.let { mediaStorageManager.deleteFile(it.absolutePath) }
        _uiState.update { state ->
            state.copy(
                audioFile = null,
                audioPart = null,
                isRecording = false,
                showDescriptionInput = state.imageParts.isNotEmpty()
            )
        }
    }

    fun requestRecommendation() {
        val state = _uiState.value
        val fId = state.farmId ?: return
        val cId = state.cropId ?: return

        val files = state.imageParts.mapNotNull { it.fileData?.fileUri } +
                listOfNotNull(state.audioPart?.fileData?.fileUri)

        viewModelScope.launch {
            _uiState.update { it.copy(isRequesting = true) }
            try {
                pesticideRepository.requestPesticideRecommendation(
                    cropId = cId,
                    farmId = fId,
                    description = description,
                    files = files
                )
                _uiState.update { it.copy(imageParts = emptyList(), audioPart = null, showDescriptionInput = false) }
                description = ""
            } catch (e: Exception) {
                _errorChannel.emit(UiText.DynamicString(e.localizedMessage ?: "Failed to request recommendation"))
            } finally {
                _uiState.update { it.copy(isRequesting = false) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}

sealed class PesticideEvent {
    data class RecommendationReceived(val id: String) : PesticideEvent()
}
