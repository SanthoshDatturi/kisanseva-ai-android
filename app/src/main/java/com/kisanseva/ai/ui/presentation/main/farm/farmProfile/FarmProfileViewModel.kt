package com.kisanseva.ai.ui.presentation.main.farm.farmProfile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.CultivatingCrop
import com.kisanseva.ai.domain.model.CurrentWeatherResponse
import com.kisanseva.ai.domain.model.FarmProfile
import com.kisanseva.ai.domain.repository.CultivatingCropRepository
import com.kisanseva.ai.domain.repository.FarmRepository
import com.kisanseva.ai.domain.repository.WeatherRepository
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FarmProfileUiState(
    val farm: FarmProfile? = null,
    val weather: CurrentWeatherResponse? = null,
    val cultivatingCrops: List<CultivatingCrop> = emptyList(),
    val isRefreshing: Boolean = false,
    val isWeatherLoading: Boolean = false,
    val isCropsRefreshing: Boolean = false,
    val selectedTabIndex: Int = 0
)

@HiltViewModel
class FarmProfileViewModel @Inject constructor(
    private val farmRepository: FarmRepository,
    private val weatherRepository: WeatherRepository,
    private val cultivatingCropRepository: CultivatingCropRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _errorChannel = MutableSharedFlow<UiText>()
    val errorChannel = _errorChannel.asSharedFlow()

    private val farmId: String = checkNotNull(savedStateHandle.get<String>("farmId"))

    init {
        observeFarmProfile()
        refreshFarmProfile()
        observeCultivatingCrops()
        refreshCultivatingCrops()
    }

    private fun observeFarmProfile() {
        viewModelScope.launch {
            farmRepository.getProfileById(farmId)
                .catch { e ->
                    _errorChannel.emit(UiText.DynamicString(e.localizedMessage ?: "An unknown error occurred"))
                }
                .filterNotNull()
                .distinctUntilChanged()
                .collectLatest { farm ->
                    _uiState.update { it.copy(farm = farm) }
                    loadWeather(farm.location.latitude, farm.location.longitude)
                }
        }
    }

    fun refreshFarmProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            when (val result = farmRepository.refreshFarmProfileById(farmId)) {
                is Result.Error -> {
                    _errorChannel.emit(result.error.asUiText())
                }
                is Result.Success -> Unit
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun loadWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWeatherLoading = true) }
            when (val result = weatherRepository.getCurrentWeather(lat, lon)) {
                is Result.Error -> {
                    _errorChannel.emit(result.error.asUiText())
                }
                is Result.Success -> {
                    _uiState.update {
                        it.copy(weather = result.data)
                    }
                }
            }
            _uiState.update { it.copy(isWeatherLoading = false) }
        }
    }

    private fun observeCultivatingCrops() {
        viewModelScope.launch {
            cultivatingCropRepository.getCultivatingCropsByFarmId(farmId)
                .catch { e ->
                    _errorChannel.emit(UiText.DynamicString(e.localizedMessage ?: "An unknown error occurred"))
                }
                .collectLatest { crops ->
                    _uiState.update { it.copy(cultivatingCrops = crops) }
                }
        }
    }

    fun refreshCultivatingCrops() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCropsRefreshing = true) }
            when (val result = cultivatingCropRepository.refreshCultivatingCropsByFarmId(farmId)) {
                is Result.Error -> {
                    _errorChannel.emit(result.error.asUiText())
                }
                is Result.Success -> Unit
            }
            _uiState.update { it.copy(isCropsRefreshing = false) }
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }
}
