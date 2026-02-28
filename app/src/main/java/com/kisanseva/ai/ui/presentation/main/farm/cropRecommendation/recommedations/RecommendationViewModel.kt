package com.kisanseva.ai.ui.presentation.main.farm.cropRecommendation.recommedations

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.InterCropRecommendation
import com.kisanseva.ai.domain.model.MonoCrop
import com.kisanseva.ai.domain.repository.CropRecommendationRepository
import com.kisanseva.ai.domain.state.Result
import com.kisanseva.ai.ui.presentation.UiText
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
import javax.inject.Inject

data class RecommendationUiState(
    val monoCrops: List<MonoCrop> = emptyList(),
    val interCrops: List<InterCropRecommendation> = emptyList(),
    val isRefreshing: Boolean = false,
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

    private val _errorChannel = MutableSharedFlow<UiText>()
    val errorChannel = _errorChannel.asSharedFlow()

    init {
        observeRecommendations()
        refreshRecommendations()
    }

    private fun observeRecommendations() {
        viewModelScope.launch {
            cropRecommendationRepository.getLatestCropRecommendation(farmId)
                .catch { e ->
                    _errorChannel.emit(UiText.DynamicString(e.localizedMessage ?: "An error occurred"))
                }
                .collectLatest { recommendation ->
                    if (recommendation != null) {
                        _uiState.update {
                            it.copy(
                                cropRecommendationResponseId = recommendation.id,
                                monoCrops = recommendation.monoCrops,
                                interCrops = recommendation.interCrops
                            )
                        }
                    }
                }
        }
    }

    fun refreshRecommendations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            when (cropRecommendationRepository.refreshCropRecommendationByFarmId(farmId)) {
                is Result.Error -> {
                    requestAndListenForRecommendations()
                }
                is Result.Success -> {
                    _uiState.update { it.copy(isRefreshing = false) }
                }
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
                                    isRefreshing = false,
                                    monoCrops = recommendation.monoCrops,
                                    interCrops = recommendation.interCrops
                                )
                            }
                        }
                    }
                    .catch { e ->
                        _errorChannel.emit(UiText.DynamicString(e.localizedMessage ?: "An error occurred"))
                        _uiState.update { it.copy(isRefreshing = false) }
                    }
                    .launchIn(viewModelScope)
            } catch (e: Exception) {
                _errorChannel.emit(UiText.DynamicString(e.localizedMessage ?: "Failed to request recommendation"))
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }
}
