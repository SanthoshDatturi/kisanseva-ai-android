package com.kisanseva.ai.ui.presentation.main.farm.cropRecommendation.cropDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.CultivatingCrop
import com.kisanseva.ai.domain.model.MonoCrop
import com.kisanseva.ai.domain.repository.CropRecommendationRepository
import com.kisanseva.ai.domain.repository.CultivatingCropRepository
import com.kisanseva.ai.domain.state.Result
import com.kisanseva.ai.ui.presentation.UiText
import com.kisanseva.ai.ui.presentation.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

data class RecommendedMonoCropDetailsState(
    val monoCrop: MonoCrop? = null,
    val isRefreshing: Boolean = false,
    val isSelectingCrop: Boolean = false,
    val progressMessages: List<String> = emptyList()
)

sealed class Event {
    data class NavigateToCultivatingCrop(val cropId: String) : Event()
}

@HiltViewModel
class RecommendedMonoCropDetailsViewModel @Inject constructor(
    private val cropRecommendationRepository: CropRecommendationRepository,
    private val cultivatingCropRepository: CultivatingCropRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(RecommendedMonoCropDetailsState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()

    private val _errorChannel = MutableSharedFlow<UiText>()
    val errorChannel = _errorChannel.asSharedFlow()

    private val monoCropId: String = checkNotNull(savedStateHandle.get<String>("monoCropId"))
    private val farmId: String = checkNotNull(savedStateHandle.get<String>("farmId"))
    private val cropRecommendationResponseId: String = checkNotNull(savedStateHandle.get<String>("cropRecommendationResponseId"))

    init {
        observeSelectionProgress()
        observeMonoCropDetails()
        refreshMonoCropDetails()
    }

    private fun observeSelectionProgress() {
        cropRecommendationRepository.listenToCropSelectionProgress()
            .onEach { message ->
                if (message.startsWith("Failed:", ignoreCase = true)) {
                    _errorChannel.emit(UiText.DynamicString(message.removePrefix("Failed:").trim()))
                    _state.update { it.copy(isSelectingCrop = false, progressMessages = emptyList()) }
                } else {
                    _state.update { current ->
                        if (!current.isSelectingCrop) return@update current
                        current.copy(
                            progressMessages = (current.progressMessages + message).distinct().takeLast(8)
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeMonoCropDetails() {
        viewModelScope.launch {
            cropRecommendationRepository.getMonoCropById(monoCropId)
                .catch { e ->
                    _errorChannel.emit(UiText.DynamicString(e.localizedMessage ?: "An error occurred"))
                }
                .collectLatest { monoCrop ->
                    _state.update { it.copy(monoCrop = monoCrop) }
                }
        }
    }

    fun refreshMonoCropDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            when (val result = cropRecommendationRepository.refreshCropRecommendationById(cropRecommendationResponseId)) {
                is Result.Error -> {
                    _errorChannel.emit(result.error.asUiText())
                }
                is Result.Success -> Unit
            }
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    fun selectCropForCultivation() {
        viewModelScope.launch {
            _state.update { it.copy(isSelectingCrop = true, progressMessages = emptyList()) }
            val monoCrop = state.value.monoCrop
            if (monoCrop == null) {
                _errorChannel.emit(UiText.DynamicString("Crop details not available to select."))
                _state.update { it.copy(isSelectingCrop = false, progressMessages = emptyList()) }
                return@launch
            }

            val cachedCultivatingCrop = cultivatingCropRepository.getCultivatingCropById(monoCropId).first()
            if (cachedCultivatingCrop != null) {
                _state.update { it.copy(isSelectingCrop = false, progressMessages = emptyList()) }
                _event.emit(Event.NavigateToCultivatingCrop(monoCropId))
                return@launch
            }

            val selectionAwaiter = async {
                withTimeoutOrNull(120_000L) {
                    cropRecommendationRepository.listenToCropSelectionResponses()
                        .first { response ->
                            response.soilHealthRecommendations.cropId == monoCropId
                        }
                }
            }

            when (val result = cropRecommendationRepository.selectCropForCultivation(
                cropId = monoCropId,
                farmId = farmId,
                cropRecommendationResponseId = cropRecommendationResponseId
            )) {
                is Result.Error -> {
                    selectionAwaiter.cancel()
                    _errorChannel.emit(result.error.asUiText())
                    _state.update { it.copy(isSelectingCrop = false, progressMessages = emptyList()) }
                }
                is Result.Success -> {
                    val selectionResult = selectionAwaiter.await()
                    if (selectionResult == null) {
                        val updatedCache = cultivatingCropRepository.getCultivatingCropById(monoCropId).first()
                        if (updatedCache != null) {
                            _state.update { it.copy(isSelectingCrop = false, progressMessages = emptyList()) }
                            _event.emit(Event.NavigateToCultivatingCrop(monoCrop.id))
                        } else {
                            _errorChannel.emit(
                                UiText.DynamicString("Selection is taking longer than expected. Please try again.")
                            )
                            _state.update { it.copy(isSelectingCrop = false, progressMessages = emptyList()) }
                        }
                    } else {
                        saveCultivatingCrop(monoCrop)
                        _state.update { it.copy(isSelectingCrop = false, progressMessages = emptyList()) }
                        _event.emit(Event.NavigateToCultivatingCrop(monoCrop.id))
                    }
                }
            }
        }
    }


    private suspend fun saveCultivatingCrop(monoCrop: MonoCrop) {
        val cultivatingCrop = CultivatingCrop(
            id = monoCrop.id,
            farmId = farmId,
            name = monoCrop.cropName,
            variety = monoCrop.variety,
            imageUrl = monoCrop.imageUrl,
            description = monoCrop.description
        )
        cultivatingCropRepository.saveCultivatingCrop(cultivatingCrop)
    }
}
