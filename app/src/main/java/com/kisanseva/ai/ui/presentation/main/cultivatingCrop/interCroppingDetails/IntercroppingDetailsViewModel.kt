package com.kisanseva.ai.ui.presentation.main.cultivatingCrop.interCroppingDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.IntercroppingDetails
import com.kisanseva.ai.domain.repository.CultivatingCropRepository
import com.kisanseva.ai.domain.state.Result
import com.kisanseva.ai.ui.presentation.UiText
import com.kisanseva.ai.ui.presentation.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IntercroppingDetailsUiState(
    val intercroppingDetails: IntercroppingDetails? = null,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class IntercroppingDetailsViewModel @Inject constructor(
    private val cultivatingCropRepository: CultivatingCropRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(IntercroppingDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private val _errorChannel = MutableSharedFlow<UiText>()
    val errorChannel = _errorChannel.asSharedFlow()

    private val intercroppingId: String = checkNotNull(savedStateHandle.get<String>("intercroppingId"))

    init {
        observeIntercroppingDetails()
        refreshIntercroppingDetails()
    }

    private fun observeIntercroppingDetails() {
        viewModelScope.launch {
            cultivatingCropRepository.getIntercroppingDetailsById(intercroppingId)
                .catch { e ->
                    _errorChannel.emit(UiText.DynamicString(e.localizedMessage ?: "An unknown error occurred"))
                }
                .collectLatest { details ->
                    _uiState.update { it.copy(intercroppingDetails = details) }
                }
        }
    }

    private fun refreshIntercroppingDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            when (val result = cultivatingCropRepository.refreshIntercroppingDetailsById(intercroppingId)) {
                is Result.Error -> {
                    _errorChannel.emit(result.error.asUiText())
                }
                is Result.Success -> Unit
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}
