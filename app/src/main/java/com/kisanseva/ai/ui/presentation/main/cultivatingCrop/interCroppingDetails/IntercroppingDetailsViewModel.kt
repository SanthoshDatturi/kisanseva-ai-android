package com.kisanseva.ai.ui.presentation.main.cultivatingCrop.interCroppingDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.IntercroppingDetails
import com.kisanseva.ai.domain.repository.CultivatingCropRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IntercroppingDetailsUiState(
    val intercroppingDetails: IntercroppingDetails? = null,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class IntercroppingDetailsViewModel @Inject constructor(
    private val cultivatingCropRepository: CultivatingCropRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(IntercroppingDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private val intercroppingId: String = checkNotNull(savedStateHandle.get<String>("intercroppingId"))

    init {
        observeIntercroppingDetails()
        refreshIntercroppingDetails()
    }

    private fun observeIntercroppingDetails() {
        viewModelScope.launch {
            cultivatingCropRepository.getIntercroppingDetailsById(intercroppingId)
                .catch { e ->
                    _uiState.update {
                        it.copy(error = e.localizedMessage ?: "An unknown error occurred")
                    }
                }
                .collectLatest { details ->
                    _uiState.update { it.copy(intercroppingDetails = details) }
                }
        }
    }

    private fun refreshIntercroppingDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            try {
                cultivatingCropRepository.refreshIntercroppingDetailsById(intercroppingId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.localizedMessage ?: "An unknown error occurred")
                }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }
}
