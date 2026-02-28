package com.kisanseva.ai.ui.presentation.main.user.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.data.local.DataStoreManager
import com.kisanseva.ai.domain.model.User
import com.kisanseva.ai.domain.repository.UserRepository
import com.kisanseva.ai.domain.state.Result
import com.kisanseva.ai.ui.presentation.UiText
import com.kisanseva.ai.ui.presentation.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isRefreshing: Boolean = false,
    val user: User? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _errorChannel = MutableSharedFlow<UiText>()
    val errorChannel = _errorChannel.asSharedFlow()

    init {
        observeUser()
        refreshUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            userRepository.getUser()
                .catch { e ->
                    _errorChannel.emit(UiText.DynamicString(e.localizedMessage ?: "An unknown error occurred"))
                }
                .collectLatest { user ->
                    _uiState.update { it.copy(user = user) }
                }
        }
    }

    fun refreshUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            if (dataStoreManager.token.first() != null) {
                when (val result = userRepository.refreshUser()) {
                    is Result.Error -> {
                        _errorChannel.emit(result.error.asUiText())
                    }
                    is Result.Success -> Unit
                }
            } else {
                _errorChannel.emit(UiText.DynamicString("User not logged in"))
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            dataStoreManager.clearToken()
            userRepository.clearUser()
        }
    }
}
