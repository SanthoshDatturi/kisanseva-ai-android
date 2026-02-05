package com.kisanseva.ai.domain.model.websocketModels

import com.kisanseva.ai.domain.model.FarmProfile
import com.kisanseva.ai.domain.model.Message
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class FarmSurveyAgentResponse(
    val command: Command,
    @SerialName("farm_profile")
    val farmProfile: FarmProfile? = null,
    @SerialName("user_message")
    val userMessage: Message? = null,
    @SerialName("model_message")
    val modelMessage: Message
)
