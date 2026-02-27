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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecommendedInterCropUiState(
    val interCrop: InterCropRecommendation? = null,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val isSelectingCrop: Boolean = false
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

    private val interCropId: String = checkNotNull(savedStateHandle.get<String>("interCropId"))
    private val farmId: String = checkNotNull(savedStateHandle.get<String>("farmId"))
    private val cropRecommendationResponseId: String = checkNotNull(savedStateHandle.get<String>("cropRecommendationResponseId"))


    init {
        observeInterCropDetails()
        refreshInterCropDetails()
    }

    private fun observeInterCropDetails() {
        viewModelScope.launch {
            cropRecommendationRepository.getInterCropById(interCropId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.localizedMessage ?: "An error occurred") }
                }
                .collectLatest { interCrop ->
                    _uiState.update { it.copy(interCrop = interCrop) }
                }
        }
    }

    fun refreshInterCropDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            try {
                cropRecommendationRepository.refreshCropRecommendationById(cropRecommendationResponseId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.localizedMessage ?: "Failed to refresh intercrop details"
                    )
                }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun selectCropForCultivation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSelectingCrop = true) }
            try {
                val interCrop = uiState.value.interCrop
                    ?: throw IllegalStateException("Intercrop details not available to select.")

                cropRecommendationRepository.selectCropForCultivation(
                    cropId = interCropId,
                    farmId = farmId,
                    cropRecommendationResponseId = cropRecommendationResponseId
                )
                saveCultivatingInterCrop(interCrop)
                _uiState.update { it.copy(isSelectingCrop = false) }
                _event.emit(InterCropEvent.NavigateToInterCroppingDetails(interCrop.id))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage ?: "An unknown error occurred.", isSelectingCrop = false) }
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
