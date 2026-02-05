package com.kisanseva.ai.ui.presentation.main.farm.cropRecommendation.recommedations

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.InterCropRecommendation
import com.kisanseva.ai.domain.model.MonoCrop
import com.kisanseva.ai.domain.repository.CropRecommendationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecommendationUiState(
    val monoCrops: List<MonoCrop> = emptyList(),
    val interCrops: List<InterCropRecommendation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val farmId: String,
    val cropRecommendationResponseId: String? = null
)

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val cropRecommendationRepository: CropRecommendationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val farmId: String = checkNotNull(savedStateHandle.get<String>("farmId"))

    private val _uiState = MutableStateFlow(RecommendationUiState(farmId = farmId))
    val uiState = _uiState.asStateFlow()

    init {
        getRecommendations()
    }

    private fun getRecommendations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val recommendations = cropRecommendationRepository.getCropRecommendationByFarmId(farmId)
                _uiState.update {
                    it.copy(
                        cropRecommendationResponseId = recommendations.id,
                        isLoading = false,
                        monoCrops = recommendations.monoCrops,
                        interCrops = recommendations.interCrops
                    )
                }
            } catch (e: Exception) {
                requestAndListenForRecommendations()
            }
        }
    }

    private fun requestAndListenForRecommendations() {
        viewModelScope.launch {
            try {
                cropRecommendationRepository.requestCropRecommendation(farmId)
                cropRecommendationRepository.listenToCropRecommendations()
                    .onEach { recommendation ->
                        if(recommendation.farmId == farmId) {
                            _uiState.update {
                                it.copy(
                                    cropRecommendationResponseId = recommendation.id,
                                    farmId = recommendation.farmId,
                                    isLoading = false,
                                    monoCrops = recommendation.monoCrops,
                                    interCrops = recommendation.interCrops
                                )
                            }
                        }
                    }
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = e.localizedMessage ?: "An error occurred"
                            )
                        }
                    }
                    .launchIn(viewModelScope)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to request recommendation"
                    )
                }
            }
        }
    }
}