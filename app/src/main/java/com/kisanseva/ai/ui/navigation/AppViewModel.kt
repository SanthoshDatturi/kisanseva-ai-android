package com.kisanseva.ai.ui.navigation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.data.local.DataStoreManager
import com.kisanseva.ai.data.remote.websocket.WebSocketController
import com.kisanseva.ai.domain.error.DataError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val webSocketController: WebSocketController
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            val token = dataStoreManager.token.first()
            _isLoggedIn.value = token != null
        }
        observeWebSocketErrors()
    }

    private val _webSocketError = MutableSharedFlow<DataError.Network>()
    val webSocketError = _webSocketError.asSharedFlow()


    private fun observeWebSocketErrors() {
        viewModelScope.launch {
            webSocketController.errors.collect {
                _webSocketError.emit(it)
            }
        }
    }
}
