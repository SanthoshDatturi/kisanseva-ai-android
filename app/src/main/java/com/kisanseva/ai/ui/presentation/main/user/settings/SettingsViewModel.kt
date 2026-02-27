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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isRefreshing: Boolean = false,
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
        observeUser()
        refreshUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            userRepository.getUser()
                .catch { e ->
                    _uiState.update { it.copy(error = e.localizedMessage) }
                }
                .collectLatest { user ->
                    _uiState.update { it.copy(user = user) }
                }
        }
    }

    fun refreshUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            try {
                if (dataStoreManager.token.first() != null) {
                    userRepository.refreshUser()
                } else {
                    _uiState.update {
                        it.copy(error = "User not logged in")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            dataStoreManager.clearToken()
            userRepository.clearUser()
        }
    }
}
