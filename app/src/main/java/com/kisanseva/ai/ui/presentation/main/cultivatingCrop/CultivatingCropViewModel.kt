package com.kisanseva.ai.ui.presentation.main.cultivatingCrop

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.CultivatingCrop
import com.kisanseva.ai.domain.model.IntercroppingDetails
import com.kisanseva.ai.domain.repository.CultivatingCropRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CultivatingCropUiState(
    val crop: CultivatingCrop? = null,
    val intercroppingDetails: IntercroppingDetails? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CultivatingCropViewModel @Inject constructor(
    private val cultivatingCropRepository: CultivatingCropRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CultivatingCropUiState())
    val uiState = _uiState.asStateFlow()

    private val cropId: String = checkNotNull(savedStateHandle.get<String>("cropId"))

    init {
        observeCultivatingCrop()
        refreshCultivatingCrop()
    }

    private fun observeCultivatingCrop() {
        viewModelScope.launch {
            cultivatingCropRepository.getCultivatingCropById(cropId)
                .catch { e ->
                    _uiState.update {
                        it.copy(error = e.localizedMessage ?: "An unknown error occurred")
                    }
                }
                .filterNotNull()
                .distinctUntilChanged()
                .collectLatest { crop ->
                    _uiState.update { it.copy(crop = crop) }
                    if (crop.intercroppingId != null) {
                        loadIntercroppingDetails(crop.intercroppingId)
                    }
                }
        }
    }

    private fun refreshCultivatingCrop() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                cultivatingCropRepository.refreshCultivatingCropById(cropId)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "An unknown error occurred"
                    )
                }
            }
        }
    }

    private fun loadIntercroppingDetails(intercroppingId: String) {
        viewModelScope.launch {
            try {
                val details = cultivatingCropRepository.getIntercroppingDetailsById(intercroppingId)
                _uiState.update { it.copy(intercroppingDetails = details) }
            } catch (e: Exception) {
                // Handle error if necessary
            }
        }
    }
}
