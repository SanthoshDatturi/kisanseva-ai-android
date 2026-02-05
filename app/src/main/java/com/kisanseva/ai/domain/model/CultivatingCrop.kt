package com.kisanseva.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CropState {
    @SerialName("selected") SELECTED,
    @SerialName("planted") PLANTED,
    @SerialName("growing") GROWING,
    @SerialName("harvested") HARVESTED,
    @SerialName("complete") COMPLETE
}

@Serializable
data class CultivatingCrop(
    val id: String,
    @SerialName("farm_id") val farmId: String,
    val name: String,
    val variety: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("crop_state") val cropState: CropState = CropState.SELECTED,
    val description: String,
    @SerialName("intercropping_id") val intercroppingId: String? = null
)

@Serializable
data class SpecificArrangement(
    @SerialName("crop_name") val cropName: String,
    val variety: String,
    val arrangement: String
)

@Serializable
data class IntercroppingDetails(
    val id: String,
    @SerialName("intercrop_type") val intercropType: String,
    @SerialName("no_of_crops") val noOfCrops: Int,
    val arrangement: String,
    @SerialName("specific_arrangement") val specificArrangement: List<SpecificArrangement>,
    val benefits: List<String>
)
