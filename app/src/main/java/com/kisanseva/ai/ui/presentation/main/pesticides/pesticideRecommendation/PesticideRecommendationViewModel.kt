package com.kisanseva.ai.ui.presentation.main.pesticides.pesticideRecommendation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.PesticideRecommendationResponse
import com.kisanseva.ai.domain.model.PesticideStage
import com.kisanseva.ai.domain.repository.PesticideRecommendationRepository
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class PesticideRecommendationUiState(
    val isRefreshing: Boolean = false,
    val recommendation: PesticideRecommendationResponse? = null,
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

    private val _errorChannel = MutableSharedFlow<UiText>()
    val errorChannel = _errorChannel.asSharedFlow()

    init {
        observeRecommendation()
        refreshRecommendation()
    }

    private fun observeRecommendation() {
        viewModelScope.launch {
            repository.getRecommendationById(recommendationId)
                .catch { e ->
                    _errorChannel.emit(UiText.DynamicString(e.localizedMessage ?: "An unknown error occurred"))
                }
                .collectLatest { response ->
                    _uiState.update { it.copy(recommendation = response) }
                }
        }
    }

    fun refreshRecommendation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            when (val result = repository.refreshRecommendationById(recommendationId)) {
                is Result.Error -> {
                    _errorChannel.emit(result.error.asUiText())
                }
                is Result.Success -> Unit
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun updateStage(pesticideId: String, stage: PesticideStage, appliedDate: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = repository.updatePesticideStage(
                recommendationId = recommendationId,
                pesticideId = pesticideId,
                stage = stage,
                appliedDate = appliedDate
            )) {
                is Result.Error -> {
                    _errorChannel.emit(result.error.asUiText())
                }
                is Result.Success -> Unit
            }
            _uiState.update { it.copy(isUpdating = false) }
        }
    }

    fun getTodayDate(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}
