package com.kisanseva.ai.domain.model.websocketModels

import com.kisanseva.ai.domain.model.FarmProfile
import com.kisanseva.ai.domain.model.Message

sealed interface ChatWebSocketEvent {
    val command: Command
    val userMessage: Message?
    val modelMessage: Message
    data class FarmSurveyEventChat(
        override val command: Command,
        val farmProfile: FarmProfile?,
        override val userMessage: Message?,
        override val modelMessage: Message
    ) : ChatWebSocketEvent
    data class GeneralChatEventChat(
        override val command: Command,
        override val userMessage: Message?,
        override val modelMessage: Message
    ) : ChatWebSocketEvent
}
