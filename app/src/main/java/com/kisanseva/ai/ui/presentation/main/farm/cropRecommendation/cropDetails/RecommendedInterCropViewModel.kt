package com.kisanseva.ai.ui.presentation.main.farm.cropRecommendation.cropDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.CultivatingCrop
import com.kisanseva.ai.domain.model.InterCropRecommendation
import com.kisanseva.ai.domain.model.IntercroppingDetails
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

data class RecommendedInterCropUiState(
    val interCrop: InterCropRecommendation? = null,
    val isRefreshing: Boolean = false,
    val isSelectingCrop: Boolean = false,
    val progressMessages: List<String> = emptyList()
)

sealed class InterCropEvent {
    data class NavigateToInterCroppingDetails(val interCropId: String) : InterCropEvent()
}

@HiltViewModel
class RecommendedInterCropViewModel @Inject constructor(
    private val cropRecommendationRepository: CropRecommendationRepository,
    private val cultivatingCropRepository: CultivatingCropRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecommendedInterCropUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<InterCropEvent>()
    val event = _event.asSharedFlow()

    private val _errorChannel = MutableSharedFlow<UiText>()
    val errorChannel = _errorChannel.asSharedFlow()

    private val interCropId: String = checkNotNull(savedStateHandle.get<String>("interCropId"))
    private val farmId: String = checkNotNull(savedStateHandle.get<String>("farmId"))
    private val cropRecommendationResponseId: String = checkNotNull(savedStateHandle.get<String>("cropRecommendationResponseId"))


    init {
        observeSelectionProgress()
        observeInterCropDetails()
        refreshInterCropDetails()
    }

    private fun observeSelectionProgress() {
        cropRecommendationRepository.listenToCropSelectionProgress()
            .onEach { message ->
                if (message.startsWith("Failed:", ignoreCase = true)) {
                    _errorChannel.emit(UiText.DynamicString(message.removePrefix("Failed:").trim()))
                    _uiState.update { it.copy(isSelectingCrop = false, progressMessages = emptyList()) }
                } else {
                    _uiState.update { current ->
                        if (!current.isSelectingCrop) return@update current
                        current.copy(
                            progressMessages = (current.progressMessages + message).distinct().takeLast(8)
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeInterCropDetails() {
        viewModelScope.launch {
            cropRecommendationRepository.getInterCropById(interCropId)
                .catch { e ->
                    _errorChannel.emit(UiText.DynamicString(e.localizedMessage ?: "An error occurred"))
                }
                .collectLatest { interCrop ->
                    _uiState.update { it.copy(interCrop = interCrop) }
                }
        }
    }

    fun refreshInterCropDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            when (val result = cropRecommendationRepository.refreshCropRecommendationById(cropRecommendationResponseId)) {
                is Result.Error -> {
                    _errorChannel.emit(result.error.asUiText())
                }
                is Result.Success -> Unit
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun selectCropForCultivation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSelectingCrop = true, progressMessages = emptyList()) }
            val interCrop = uiState.value.interCrop
            if (interCrop == null) {
                _errorChannel.emit(UiText.DynamicString("Intercrop details not available to select."))
                _uiState.update { it.copy(isSelectingCrop = false, progressMessages = emptyList()) }
                return@launch
            }

            val cachedIntercrop = cultivatingCropRepository.getIntercroppingDetailsById(interCropId).first()
            if (cachedIntercrop != null) {
                _uiState.update { it.copy(isSelectingCrop = false, progressMessages = emptyList()) }
                _event.emit(InterCropEvent.NavigateToInterCroppingDetails(interCropId))
                return@launch
            }

            val selectionAwaiter = async {
                withTimeoutOrNull(120_000L) {
                    cropRecommendationRepository.listenToCropSelectionResponses()
                        .first { response ->
                            response.soilHealthRecommendations.cropId == interCropId
                        }
                }
            }

            when (val result = cropRecommendationRepository.selectCropForCultivation(
                cropId = interCropId,
                farmId = farmId,
                cropRecommendationResponseId = cropRecommendationResponseId
            )) {
                is Result.Error -> {
                    selectionAwaiter.cancel()
                    _errorChannel.emit(result.error.asUiText())
                    _uiState.update { it.copy(isSelectingCrop = false, progressMessages = emptyList()) }
                }
                is Result.Success -> {
                    val selectionResult = selectionAwaiter.await()
                    if (selectionResult == null) {
                        val updatedCache = cultivatingCropRepository.getIntercroppingDetailsById(interCropId).first()
                        if (updatedCache != null) {
                            _uiState.update { it.copy(isSelectingCrop = false, progressMessages = emptyList()) }
                            _event.emit(InterCropEvent.NavigateToInterCroppingDetails(interCrop.id))
                        } else {
                            _errorChannel.emit(
                                UiText.DynamicString("Selection is taking longer than expected. Please try again.")
                            )
                            _uiState.update { it.copy(isSelectingCrop = false, progressMessages = emptyList()) }
                        }
                    } else {
                        saveCultivatingInterCrop(interCrop)
                        _uiState.update { it.copy(isSelectingCrop = false, progressMessages = emptyList()) }
                        _event.emit(InterCropEvent.NavigateToInterCroppingDetails(interCrop.id))
                    }
                }
            }
        }
    }

    private suspend fun saveCultivatingMonoCrop(monoCrop: MonoCrop) {
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

    private suspend fun saveCultivatingInterCrop(interCrop: InterCropRecommendation) {
        val intercroppingDetails = IntercroppingDetails(
            id = interCrop.id,
            intercropType = interCrop.intercropType,
            noOfCrops = interCrop.noOfCrops,
            arrangement = interCrop.arrangement,
            specificArrangement = interCrop.specificArrangement,
            benefits = interCrop.benefits
        )

        interCrop.crops.forEach {
            saveCultivatingMonoCrop(it)
        }

        cultivatingCropRepository.saveIntercroppingDetails(intercroppingDetails)
    }
}
