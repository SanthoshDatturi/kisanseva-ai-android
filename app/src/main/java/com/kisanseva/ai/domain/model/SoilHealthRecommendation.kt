package com.kisanseva.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImmediateAction(
    val parameter: String,
    val recommendation: String,
    val product: String,
    val cost: String
)

@Serializable
data class SoilHealthRecommendations(
    val id: String,
    @SerialName("crop_id") val cropId: String,
    @SerialName("immediate_actions") val immediateActions: List<ImmediateAction>,
    val description: String,
    @SerialName("long_term_improvements") val longTermImprovements: List<String>
)
