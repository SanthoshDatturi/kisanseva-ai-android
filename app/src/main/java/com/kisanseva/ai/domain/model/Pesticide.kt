package com.kisanseva.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PesticideStage {
    @SerialName("recommended") RECOMMENDED,
    @SerialName("selected") SELECTED,
    @SerialName("applied") APPLIED
}

@Serializable
enum class PesticideType {
    @SerialName("chemical") CHEMICAL,
    @SerialName("organic") ORGANIC,
    @SerialName("biological") BIOLOGICAL
}

@Serializable
data class PesticideRecommendationRequestData(
    @SerialName("crop_id") val cropId: String,
    @SerialName("farm_id") val farmId: String,
    @SerialName("pest_or_disease_description") val pestOrDiseaseDescription: String,
    val files: List<String> = emptyList()
)

@Serializable
data class PesticideStageUpdateRequest(
    @SerialName("pesticide_id")
    val pesticideId: String,
    val stage: PesticideStage,
    @SerialName("applied_date")
    val appliedDate: String? = null
)

@Serializable
data class PesticideInfo(
    @SerialName("_id")
    val id: String,
    @SerialName("pesticide_name")
    val pesticideName: String,
    @SerialName("pesticide_type")
    val pesticideType: PesticideType,
    val dosage: String,
    @SerialName("application_method")
    val applicationMethod: String,
    val precautions: List<String>,
    val explanation: String,
    val rank: Int,
    val stage: PesticideStage = PesticideStage.RECOMMENDED,
    @SerialName("applied_date")
    val appliedDate: String? = null
)

@Serializable
data class PesticideRecommendationResponse(
    @SerialName("_id")
    val id: String,
    @SerialName("farm_id")
    val farmId: String?,
    @SerialName("crop_id")
    val cropId: String?,
    val timestamp: Double,
    @SerialName("disease_details")
    val diseaseDetails: String,
    val recommendations: List<PesticideInfo>,
    @SerialName("general_advice")
    val generalAdvice: String
)

@Serializable
data class PesticideRecommendationError(
    val reason: String,
    @SerialName("suggest_input_changes")
    val suggestInputChanges: String
)
