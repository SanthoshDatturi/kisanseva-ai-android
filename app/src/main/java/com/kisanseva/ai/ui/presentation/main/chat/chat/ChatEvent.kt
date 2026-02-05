package com.kisanseva.ai.ui.presentation.main.chat.chat

import com.kisanseva.ai.domain.model.websocketModels.Command

sealed interface ChatEvent {
    data class HandleCommand<T>(val command: Command, val data: T? = null) : ChatEvent
}
