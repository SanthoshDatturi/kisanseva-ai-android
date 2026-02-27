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
    val isRefreshing: Boolean = false,
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
                        observeIntercroppingDetails(crop.intercroppingId)
                    }
                }
        }
    }

    private fun refreshCultivatingCrop() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            try {
                cultivatingCropRepository.refreshCultivatingCropById(cropId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.localizedMessage ?: "An unknown error occurred")
                }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun observeIntercroppingDetails(intercroppingId: String) {
        viewModelScope.launch {
            cultivatingCropRepository.getIntercroppingDetailsById(intercroppingId)
                .catch { e ->
                    // Handle error if necessary
                }
                .collectLatest { details ->
                    _uiState.update { it.copy(intercroppingDetails = details) }
                }
        }
    }
}
