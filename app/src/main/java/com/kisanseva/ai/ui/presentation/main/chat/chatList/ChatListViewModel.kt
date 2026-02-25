package com.kisanseva.ai.ui.presentation.main.chat.chatList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanseva.ai.domain.model.ChatSession
import com.kisanseva.ai.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatListUiState(
    val chatSessions: List<ChatSession> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        observeChatSessions()
        refreshChatSessions()
    }

    private fun observeChatSessions() {
        viewModelScope.launch {
            chatRepository.getChatSessions()
                .catch { e ->
                    _uiState.update { it.copy(error = e.localizedMessage ?: "An unknown error occurred") }
                }
                .collectLatest { sessions ->
                    _uiState.update { it.copy(chatSessions = sessions) }
                }
        }
    }

    fun refreshChatSessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                chatRepository.refreshChatSessions()
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

    fun deleteChatSession(chatId: String) {
        viewModelScope.launch {
            try {
                chatRepository.deleteChatSession(chatId)
            } catch (e: Exception) {
                 _uiState.update {
                    it.copy(error = e.localizedMessage ?: "Failed to delete chat")
                }
            }
        }
    }
}
