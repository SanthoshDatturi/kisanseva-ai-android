package com.kisanseva.ai.ui.presentation.main.investment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.InvestmentBreakdown
import com.kisanseva.ai.domain.repository.InvestmentBreakdownRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvestmentUiState(
    val breakdown: InvestmentBreakdown? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class InvestmentBreakdownViewModel @Inject constructor(
    private val repository: InvestmentBreakdownRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvestmentUiState())
    val uiState = _uiState.asStateFlow()

    private val cropId: String? = savedStateHandle.get<String>("cropId")
    private val breakdownId: String? = savedStateHandle.get<String>("breakdownId")

    init {
        loadBreakdown()
    }

    private fun loadBreakdown() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = when {
                    breakdownId != null -> repository.getBreakdownById(breakdownId)
                    cropId != null -> repository.getBreakdownByCropId(cropId)
                    else -> throw IllegalArgumentException("Missing cropId or breakdownId")
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        breakdown = result
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to load investment breakdown"
                    )
                }
            }
        }
    }
}
