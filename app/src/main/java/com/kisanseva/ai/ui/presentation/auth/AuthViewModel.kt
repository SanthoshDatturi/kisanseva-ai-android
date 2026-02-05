package com.kisanseva.ai.ui.presentation.auth

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.R
import com.kisanseva.ai.domain.model.OTPSendRequest
import com.kisanseva.ai.domain.model.OTPVerifyRequest
import com.kisanseva.ai.domain.repository.AuthRepository
import com.kisanseva.ai.exception.ApiException
import com.kisanseva.ai.exception.ExceptionCodes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

sealed interface AuthUIState {
    data object Idle : AuthUIState
    data object Loading : AuthUIState
    data object OTPSent : AuthUIState
    data object OTPVerified : AuthUIState
    data class Error(val code: Int) : AuthUIState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUIState>(AuthUIState.Idle)
    val uiState: StateFlow<AuthUIState> = _uiState.asStateFlow()

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
            try {
                repository.sendOtp(OTPSendRequest(phone, name, language))
                _uiState.value = AuthUIState.OTPSent
            } catch (e: ApiException) {
                _uiState.value = AuthUIState.Error(e.code)
            } catch (e: IOException) {
                _uiState.value = AuthUIState.Error(ExceptionCodes.NO_INTERNET_CODE)
            } catch (e: Exception) {
                _uiState.value = AuthUIState.Error(ExceptionCodes.UNKNOWN_ERROR_CODE)
            }
        }
    }

    fun verifyOtp(phone: String, otp: String) {
        viewModelScope.launch {
            _uiState.value = AuthUIState.Loading
            try {
                repository.verifyOtp(OTPVerifyRequest(phone, otp))
                _uiState.value = AuthUIState.OTPVerified
            } catch (e: ApiException) {
                _uiState.value = AuthUIState.Error(e.code)
            } catch (e: IOException) {
                _uiState.value = AuthUIState.Error(ExceptionCodes.NO_INTERNET_CODE)
            } catch (e: Exception) {
                _uiState.value = AuthUIState.Error(ExceptionCodes.UNKNOWN_ERROR_CODE)
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUIState.Idle
    }
}

@StringRes
fun mapErrorCodeToStringResource(code: Int): Int {
    return when (code) {
        ExceptionCodes.USER_NOT_FOUND -> R.string.user_not_found
        ExceptionCodes.INVALID_OTP -> R.string.invalid_otp
        ExceptionCodes.USER_ALREADY_EXISTS -> R.string.user_already_exists
        ExceptionCodes.NO_INTERNET_CODE -> R.string.no_internet
        ExceptionCodes.INTERNAL_SERVER_ERROR -> R.string.internal_server_error
        else -> R.string.unknown_error
    }
}
