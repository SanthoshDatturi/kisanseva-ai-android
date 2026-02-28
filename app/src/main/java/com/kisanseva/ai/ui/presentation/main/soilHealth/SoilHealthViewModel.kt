package com.kisanseva.ai.ui.presentation.main.soilHealth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.SoilHealthRecommendations
import com.kisanseva.ai.domain.repository.SoilHealthRecommendationRepository
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
import javax.inject.Inject

data class SoilHealthUiState(
    val recommendations: List<SoilHealthRecommendations> = emptyList(),
    val isRefreshing: Boolean = false
)

@HiltViewModel
class SoilHealthViewModel @Inject constructor(
    private val repository: SoilHealthRecommendationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoilHealthUiState())
    val uiState = _uiState.asStateFlow()

    private val _errorChannel = MutableSharedFlow<UiText>()
    val errorChannel = _errorChannel.asSharedFlow()

    private val cropId: String? = savedStateHandle.get<String>("cropId")
    private val recommendationId: String? = savedStateHandle.get<String>("recommendationId")

    init {
        observeRecommendations()
        refreshRecommendations()
    }

    private fun observeRecommendations() {
        viewModelScope.launch {
            when {
                recommendationId != null -> repository.getRecommendationById(recommendationId).catch { e ->
                    _errorChannel.emit(UiText.DynamicString(e.localizedMessage ?: "An unknown error occurred"))
                }.collectLatest { result ->
                    _uiState.update { it.copy(recommendations = listOfNotNull(result)) }
                }
                cropId != null -> repository.getRecommendationsByCropId(cropId).catch { e ->
                    _errorChannel.emit(UiText.DynamicString(e.localizedMessage ?: "An unknown error occurred"))
                }.collectLatest { results ->
                    _uiState.update { it.copy(recommendations = results) }
                }
            }
        }
    }

    fun refreshRecommendations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val result = when {
                recommendationId != null -> repository.refreshRecommendationById(recommendationId)
                cropId != null -> repository.refreshRecommendationsByCropId(cropId)
                else -> {
                    _errorChannel.emit(UiText.DynamicString("Missing cropId or recommendationId"))
                    null
                }
            }
            
            if (result is Result.Error) {
                _errorChannel.emit(result.error.asUiText())
            }
            
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}
