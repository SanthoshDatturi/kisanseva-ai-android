package com.kisanseva.ai.ui.presentation.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.CropState
import com.kisanseva.ai.domain.model.CultivatingCrop
import com.kisanseva.ai.domain.repository.CultivatingCropRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val cultivatingCrops: List<CultivatingCrop> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cultivatingCropRepository: CultivatingCropRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeCultivatingCrops()
        refreshCultivatingCrops()
    }

    private fun observeCultivatingCrops() {
        viewModelScope.launch {
            cultivatingCropRepository.getAllCultivatingCrops()
                .catch { e ->
                    _uiState.update {
                        it.copy(error = e.localizedMessage ?: "An unknown error occurred")
                    }
                }
                .collectLatest { crops ->
                    _uiState.update {
                        it.copy(cultivatingCrops = crops.filter { it.cropState != CropState.COMPLETE })
                    }
                }
        }
    }

    fun refreshCultivatingCrops() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            try {
                cultivatingCropRepository.refreshAllCultivatingCrops()
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
