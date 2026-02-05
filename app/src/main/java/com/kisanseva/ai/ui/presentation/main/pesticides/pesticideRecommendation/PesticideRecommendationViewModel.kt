package com.kisanseva.ai.ui.presentation.main.pesticides.pesticideRecommendation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.PesticideRecommendationResponse
import com.kisanseva.ai.domain.model.PesticideStage
import com.kisanseva.ai.domain.repository.PesticideRecommendationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class PesticideRecommendationUiState(
    val isLoading: Boolean = false,
    val recommendation: PesticideRecommendationResponse? = null,
    val error: String? = null,
    val isUpdating: Boolean = false
)

@HiltViewModel
class PesticideRecommendationViewModel @Inject constructor(
    private val repository: PesticideRecommendationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recommendationId: String = checkNotNull(savedStateHandle["recommendationId"])

    private val _uiState = MutableStateFlow(PesticideRecommendationUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadRecommendation()
    }

    fun loadRecommendation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = repository.getRecommendationById(recommendationId)
                _uiState.update { it.copy(isLoading = false, recommendation = response) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun updateStage(pesticideId: String, stage: PesticideStage, appliedDate: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            try {
                repository.updatePesticideStage(
                    recommendationId = recommendationId,
                    pesticideId = pesticideId,
                    stage = stage,
                    appliedDate = appliedDate
                )
                loadRecommendation() // Refresh data
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage) }
            } finally {
                _uiState.update { it.copy(isUpdating = false) }
            }
        }
    }

    fun getTodayDate(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}
