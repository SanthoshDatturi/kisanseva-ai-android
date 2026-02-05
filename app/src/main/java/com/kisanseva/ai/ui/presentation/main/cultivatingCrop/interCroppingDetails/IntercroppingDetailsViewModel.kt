package com.kisanseva.ai.ui.presentation.main.cultivatingCrop.interCroppingDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.IntercroppingDetails
import com.kisanseva.ai.domain.repository.CultivatingCropRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IntercroppingDetailsUiState(
    val intercroppingDetails: IntercroppingDetails? = null,
    val isLoading: Boolean = false,
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
        loadIntercroppingDetails()
    }

    private fun loadIntercroppingDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val details = cultivatingCropRepository.getIntercroppingDetailsById(intercroppingId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        intercroppingDetails = details
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