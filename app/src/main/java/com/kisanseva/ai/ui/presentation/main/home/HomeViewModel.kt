package com.kisanseva.ai.ui.presentation.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.CropState
import com.kisanseva.ai.domain.model.CultivatingCrop
import com.kisanseva.ai.domain.repository.CultivatingCropRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val cultivatingCrops: List<CultivatingCrop> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cultivatingCropRepository: CultivatingCropRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCultivatingCrops()
    }

    private fun loadCultivatingCrops() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val crops = cultivatingCropRepository.getAllCultivatingCrops()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        cultivatingCrops = crops.filter { it.cropState != CropState.COMPLETE }
                    )
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