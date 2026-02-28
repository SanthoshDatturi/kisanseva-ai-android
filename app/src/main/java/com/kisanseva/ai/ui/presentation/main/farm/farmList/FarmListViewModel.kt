package com.kisanseva.ai.ui.presentation.main.farm.farmList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.FarmProfile
import com.kisanseva.ai.domain.repository.FarmRepository
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


data class FarmListUiState(
    val farms: List<FarmProfile> = emptyList(),
    val isRefreshing: Boolean = false
)

@HiltViewModel
class FarmListViewModel @Inject constructor(
    private val farmRepository: FarmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmListUiState())
    val uiState = _uiState.asStateFlow()

    private val _errorChannel = MutableSharedFlow<UiText>()
    val errorChannel = _errorChannel.asSharedFlow()

    init {
        observeFarms()
        refreshFarms()
    }

    private fun observeFarms() {
        viewModelScope.launch {
            farmRepository.getFarmProfiles()
                .catch { e ->
                    _errorChannel.emit(UiText.DynamicString(e.localizedMessage ?: "An unknown error occurred"))
                }
                .collectLatest { farms ->
                    _uiState.update { it.copy(farms = farms) }
                }
        }
    }

    fun refreshFarms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            when (val result = farmRepository.refreshFarmProfiles()) {
                is Result.Error -> {
                    _errorChannel.emit(result.error.asUiText())
                }
                is Result.Success -> Unit
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}
