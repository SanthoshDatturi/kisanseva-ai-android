package com.kisanseva.ai.ui.presentation.main.farm.cropRecommendation.cropDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.CultivatingCrop
import com.kisanseva.ai.domain.model.MonoCrop
import com.kisanseva.ai.domain.repository.CropRecommendationRepository
import com.kisanseva.ai.domain.repository.CultivatingCropRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecommendedMonoCropDetailsState(
    val monoCrop: MonoCrop? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSelectingCrop: Boolean = false
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

    private val monoCropId: String = checkNotNull(savedStateHandle.get<String>("monoCropId"))
    private val farmId: String = checkNotNull(savedStateHandle.get<String>("farmId"))
    private val cropRecommendationResponseId: String = checkNotNull(savedStateHandle.get<String>("cropRecommendationResponseId"))

    init {
        getMonoCropDetails(monoCropId)
    }

    private fun getMonoCropDetails(monoCropId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val monoCrop = cropRecommendationRepository.getMonoCropById(monoCropId)
                _state.update { it.copy(monoCrop = monoCrop, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.localizedMessage, isLoading = false) }
            }
        }
    }

    fun selectCropForCultivation() {
        viewModelScope.launch {
            _state.update { it.copy(isSelectingCrop = true) }
            try {
                val monoCrop = state.value.monoCrop
                    ?: throw IllegalStateException("Crop details not available to select.")

                cropRecommendationRepository.selectCropForCultivation(
                    cropId = monoCropId,
                    farmId = farmId,
                    cropRecommendationResponseId = cropRecommendationResponseId
                )
                saveCultivatingCrop(monoCrop)
                _state.update { it.copy(isSelectingCrop = false) }
                _event.emit(Event.NavigateToCultivatingCrop(monoCrop.id))
            } catch (e: Exception) {
                _state.update { it.copy(error = e.localizedMessage, isSelectingCrop = false) }
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