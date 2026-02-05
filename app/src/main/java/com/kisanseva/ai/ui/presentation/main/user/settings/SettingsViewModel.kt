package com.kisanseva.ai.ui.presentation.main.user.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.data.local.DataStoreManager
import com.kisanseva.ai.domain.model.User
import com.kisanseva.ai.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        getProfile()
    }

    private fun getProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState(isLoading = true)
            try {
                if (dataStoreManager.token.first() != null) {
                    val user = userRepository.getProfile()
                    _uiState.value = ProfileUiState(user = user)
                } else {
                    _uiState.value = ProfileUiState(error = "User not logged in")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState(error = e.message)
            }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            dataStoreManager.clearToken()
        }
    }
}
