package com.kisanseva.ai.ui.presentation.main.soilHealth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.SoilHealthRecommendations
import com.kisanseva.ai.domain.repository.SoilHealthRecommendationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SoilHealthUiState(
    val recommendations: List<SoilHealthRecommendations> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SoilHealthViewModel @Inject constructor(
    private val repository: SoilHealthRecommendationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoilHealthUiState())
    val uiState = _uiState.asStateFlow()

    private val cropId: String? = savedStateHandle.get<String>("cropId")
    private val recommendationId: String? = savedStateHandle.get<String>("recommendationId")

    init {
        loadRecommendations()
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val results = when {
                    recommendationId != null -> listOf(repository.getRecommendationById(recommendationId))
                    cropId != null -> repository.getRecommendationsByCropId(cropId)
                    else -> throw IllegalArgumentException("Missing cropId or recommendationId")
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        recommendations = results
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to load recommendations"
                    )
                }
            }
        }
    }
}
