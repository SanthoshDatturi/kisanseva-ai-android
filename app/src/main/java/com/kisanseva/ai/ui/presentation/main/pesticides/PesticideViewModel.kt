package com.kisanseva.ai.ui.presentation.main.pesticides

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.data.local.DataStoreManager
import com.kisanseva.ai.domain.model.CultivatingCrop
import com.kisanseva.ai.domain.model.FarmProfile
import com.kisanseva.ai.domain.model.FileData
import com.kisanseva.ai.domain.model.FileFolder
import com.kisanseva.ai.domain.model.FileType
import com.kisanseva.ai.domain.model.Part
import com.kisanseva.ai.domain.model.PesticideInfo
import com.kisanseva.ai.domain.model.PesticideStage
import com.kisanseva.ai.domain.repository.CultivatingCropRepository
import com.kisanseva.ai.domain.repository.FarmRepository
import com.kisanseva.ai.domain.repository.FilesRepository
import com.kisanseva.ai.domain.repository.PesticideRecommendationRepository
import com.kisanseva.ai.system.audio.player.AudioPlayer
import com.kisanseva.ai.system.storage.MediaStorageManager
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
    val isLoading: Boolean = false,
    val farms: List<FarmProfile> = emptyList(),
    val selectedFarmId: String? = null,
    val cultivatingCrops: List<CultivatingCrop> = emptyList(),
    val selectedCropId: String? = null,
    val previousPesticides: List<Pair<String, PesticideInfo>> = emptyList(), // recommendationId to PesticideInfo
    val isUploading: Boolean = false,
    val error: String? = null,
    val imageParts: List<Part> = emptyList(),
    val isRecording: Boolean = false,
    val audioFile: File? = null,
    val audioPart: Part? = null,
    val showDescriptionInput: Boolean = false
)

@HiltViewModel
class PesticideViewModel @Inject constructor(
    private val farmRepository: FarmRepository,
    private val cultivatingCropRepository: CultivatingCropRepository,
    private val pesticideRepository: PesticideRecommendationRepository,
    private val filesRepository: FilesRepository,
    private val mediaStorageManager: MediaStorageManager,
    private val dataStoreManager: DataStoreManager,
    val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(PesticideUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PesticideEvent>()
    val events = _events.asSharedFlow()

    var description by mutableStateOf("")
        private set

    private var cropsJob: Job? = null
    private var pesticidesJob: Job? = null

    init {
        observeFarms()
        refreshFarms()
        observeRecommendations()
    }

    private fun observeFarms() {
        viewModelScope.launch {
            farmRepository.getFarmProfiles()
                .catch { e ->
                    _uiState.update { it.copy(error = e.localizedMessage) }
                }
                .collectLatest { farms ->
                    _uiState.update { it.copy(farms = farms) }
                    if (farms.isNotEmpty() && _uiState.value.selectedFarmId == null) {
                        selectFarm(farms.first().id)
                    }
                }
        }
    }

    private fun refreshFarms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                farmRepository.refreshFarmProfiles()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    private fun observeRecommendations() {
        pesticideRepository.listenToPesticideRecommendations()
            .onEach { recommendation ->
                val cropId = recommendation.cropId
                if (cropId != null && cropId == _uiState.value.selectedCropId) {
                    _events.emit(PesticideEvent.RecommendationReceived(recommendation.id))
                }
            }
            .launchIn(viewModelScope)
    }

    fun selectFarm(farmId: String) {
        _uiState.update { it.copy(selectedFarmId = farmId, selectedCropId = null, previousPesticides = emptyList()) }
        observeCrops(farmId)
        refreshCrops(farmId)
    }

    private fun observeCrops(farmId: String) {
        cropsJob?.cancel()
        cropsJob = viewModelScope.launch {
            cultivatingCropRepository.getCultivatingCropsByFarmId(farmId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.localizedMessage) }
                }
                .collectLatest { crops ->
                    _uiState.update { it.copy(cultivatingCrops = crops) }
                    if (crops.isNotEmpty() && _uiState.value.selectedCropId == null) {
                        selectCrop(crops.first().id)
                    }
                }
        }
    }

    private fun refreshCrops(farmId: String) {
        viewModelScope.launch {
            try {
                cultivatingCropRepository.refreshCultivatingCropsByFarmId(farmId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage) }
            }
        }
    }

    fun selectCrop(cropId: String) {
        _uiState.update { it.copy(selectedCropId = cropId) }
        observePreviousPesticides(cropId)
        refreshPreviousPesticides(cropId)
    }

    private fun observePreviousPesticides(cropId: String) {
        pesticidesJob?.cancel()
        pesticidesJob = viewModelScope.launch {
            pesticideRepository.getRecommendationsByCropId(cropId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.localizedMessage) }
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

    private fun refreshPreviousPesticides(cropId: String) {
        viewModelScope.launch {
            try {
                pesticideRepository.refreshRecommendationsByCropId(cropId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage) }
            }
        }
    }

    fun onDescriptionChange(newDescription: String) {
        description = newDescription
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
                    blobName = UUID.randomUUID().toString(),
                    fileType = FileType.USER_CONTENT,
                    mimeType = mimeType,
                    folder = FileFolder.IMAGES,
                    pathPrefix = null
                )
                _uiState.update { state ->
                    val updatedParts = state.imageParts.map {
                        if (it.localId == newPart.localId) {
                            it.copy(fileData = it.fileData?.copy(fileUri = response.url))
                        } else it
                    }
                    state.copy(imageParts = updatedParts, showDescriptionInput = true)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage) }
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
                val newPart = Part(
                    fileData = FileData(mimeType = "audio/mp4", localUri = audioFile.absolutePath),
                )
                _uiState.update { it.copy(audioPart = newPart) }

                val response = filesRepository.uploadFile(
                    fileStream = audioFile.inputStream(),
                    blobName = UUID.randomUUID().toString(),
                    fileType = FileType.USER_CONTENT,
                    mimeType = "audio/mp4",
                    folder = FileFolder.AUDIO,
                    pathPrefix = null
                )

                _uiState.update { state ->
                    if (state.audioPart?.localId == newPart.localId) {
                        state.copy(
                            audioPart = state.audioPart.copy(fileData = state.audioPart.fileData?.copy(fileUri = response.url)),
                            showDescriptionInput = true
                        )
                    } else state
                }
            } catch (e: Exception) {
                mediaStorageManager.deleteFile(audioFile.absolutePath)
                _uiState.update { it.copy(error = "Audio upload failed", audioPart = null) }
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
        val fId = state.selectedFarmId ?: return
        val cId = state.selectedCropId ?: return

        val files = state.imageParts.mapNotNull { it.fileData?.fileUri } +
                listOfNotNull(state.audioPart?.fileData?.fileUri)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                pesticideRepository.requestPesticideRecommendation(
                    cropId = cId,
                    farmId = fId,
                    description = description,
                    files = files
                )
                _uiState.update { it.copy(imageParts = emptyList(), audioPart = null, showDescriptionInput = false, isLoading = false) }
                description = ""
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
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
