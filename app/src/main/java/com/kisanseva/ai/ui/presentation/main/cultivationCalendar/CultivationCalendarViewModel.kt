package com.kisanseva.ai.ui.presentation.main.cultivationCalendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.CultivationCalendar
import com.kisanseva.ai.domain.repository.CultivatingCalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CultivationCalendarUiState(
    val calendar: CultivationCalendar? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CultivationCalendarViewModel @Inject constructor(
    private val repository: CultivatingCalendarRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CultivationCalendarUiState())
    val uiState = _uiState.asStateFlow()

    private val cropId: String? = savedStateHandle.get<String>("cropId")
    private val calendarId: String? = savedStateHandle.get<String>("calendarId")

    init {
        observeCalendar()
        refreshCalendar()
    }

    private fun observeCalendar() {
        viewModelScope.launch {
            val flow = when {
                calendarId != null -> repository.getCalendarById(calendarId)
                cropId != null -> repository.getCalendarByCropId(cropId)
                else -> return@launch
            }
            flow.catch { e ->
                _uiState.update { it.copy(error = e.localizedMessage ?: "An error occurred") }
            }.collectLatest { calendar ->
                _uiState.update { it.copy(calendar = calendar) }
            }
        }
    }

    private fun refreshCalendar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                when {
                    calendarId != null -> repository.refreshCalendarById(calendarId)
                    cropId != null -> repository.refreshCalendarByCropId(cropId)
                    else -> throw IllegalArgumentException("Neither cropId nor calendarId provided")
                }
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
}
