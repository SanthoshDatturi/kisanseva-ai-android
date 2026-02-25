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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
    val isLoading: Boolean = false,
    val isWeatherLoading: Boolean = false,
    val isCropsLoading: Boolean = false,
    val error: String? = null,
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
                    _uiState.update {
                        it.copy(error = e.localizedMessage ?: "An unknown error occurred")
                    }
                }
                .filterNotNull()
                .distinctUntilChanged()
                .collectLatest { farm ->
                    _uiState.update { it.copy(farm = farm) }
                    loadWeather(farm.location.latitude, farm.location.longitude)
                }
        }
    }

    private fun refreshFarmProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                farmRepository.refreshFarmProfileById(farmId)
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

    private fun loadWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWeatherLoading = true) }
            try {
                val weather = weatherRepository.getCurrentWeather(lat, lon)
                _uiState.update {
                    it.copy(
                        isWeatherLoading = false,
                        weather = weather
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isWeatherLoading = false) }
            }
        }
    }

    private fun observeCultivatingCrops() {
        viewModelScope.launch {
            cultivatingCropRepository.getCultivatingCropsByFarmId(farmId)
                .catch { e ->
                    _uiState.update {
                        it.copy(error = e.localizedMessage ?: "An unknown error occurred")
                    }
                }
                .collectLatest { crops ->
                    _uiState.update { it.copy(cultivatingCrops = crops) }
                }
        }
    }

    private fun refreshCultivatingCrops() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCropsLoading = true) }
            try {
                cultivatingCropRepository.refreshCultivatingCropsByFarmId(farmId)
                _uiState.update { it.copy(isCropsLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCropsLoading = false,
                        error = e.localizedMessage ?: "An unknown error occurred"
                    )
                }
            }
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }
}
