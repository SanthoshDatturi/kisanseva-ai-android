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
        loadCultivatingCrop()
    }

    private fun loadCultivatingCrop() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val crop = cultivatingCropRepository.getCultivatingCropById(cropId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        crop = crop
                    )
                }
                if (crop.intercroppingId != null) {
                    val intercroppingDetails = cultivatingCropRepository.getIntercroppingDetailsById(crop.intercroppingId)
                    _uiState.update { it.copy(intercroppingDetails = intercroppingDetails) }
                }
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
}
