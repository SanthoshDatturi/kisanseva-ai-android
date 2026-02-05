package com.kisanseva.ai.ui.presentation.main.farm.farmList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.FarmProfile
import com.kisanseva.ai.domain.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class FarmListUiState(
    val farms: List<FarmProfile> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FarmListViewModel @Inject constructor(
    private val farmRepository: FarmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadFarms()
    }

    private fun loadFarms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val farms = farmRepository.getFarmProfiles()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        farms = farms
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
