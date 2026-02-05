package com.kisanseva.ai.domain.model.websocketModels

import com.kisanseva.ai.domain.model.Message
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class GeneralChatResponse(
    val command: Command,
    @SerialName("user_message")
    val userMessage: Message? = null,
    @SerialName("model_message")
    val modelMessage: Message
)