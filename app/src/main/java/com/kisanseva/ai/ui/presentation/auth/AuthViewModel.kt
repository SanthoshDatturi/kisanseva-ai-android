package com.kisanseva.ai.ui.presentation.auth

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.OTPSendRequest
import com.kisanseva.ai.domain.model.OTPVerifyRequest
import com.kisanseva.ai.domain.repository.AuthRepository
import com.kisanseva.ai.domain.state.Result
import com.kisanseva.ai.ui.presentation.UiText
import com.kisanseva.ai.ui.presentation.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthUIState {
    data object Idle : AuthUIState
    data object Loading : AuthUIState
    data object OTPSent : AuthUIState
    data object OTPVerified : AuthUIState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUIState>(AuthUIState.Idle)
    val uiState: StateFlow<AuthUIState> = _uiState.asStateFlow()

    private val _errorChannel = MutableSharedFlow<UiText>()
    val errorChannel = _errorChannel.asSharedFlow()

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language

    init {
        viewModelScope.launch {
            val locales = AppCompatDelegate.getApplicationLocales()
            if (!locales.isEmpty) {
                _language.value = locales[0]?.language ?: "en"
            } else {
                _language.value = "en"
            }
        }
    }

    fun sendOtp(phone: String, name: String? = null, language: String? = null) {
        viewModelScope.launch {
            _uiState.value = AuthUIState.Loading
            when (val result = repository.sendOtp(OTPSendRequest(phone, name, language))) {
                is Result.Error -> {
                    _uiState.value = AuthUIState.Idle
                    _errorChannel.emit(result.error.asUiText())
                }

                is Result.Success -> {
                    _uiState.value = AuthUIState.OTPSent
                }
            }
        }
    }

    fun verifyOtp(phone: String, otp: String) {
        viewModelScope.launch {
            _uiState.value = AuthUIState.Loading
            when (val result = repository.verifyOtp(OTPVerifyRequest(phone, otp))) {
                is Result.Error -> {
                    _uiState.value = AuthUIState.Idle
                    _errorChannel.emit(result.error.asUiText())
                }

                is Result.Success -> {
                    _uiState.value = AuthUIState.OTPVerified
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUIState.Idle
    }
}
