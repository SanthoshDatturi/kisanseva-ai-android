package com.kisanseva.ai.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.kisanseva.ai.domain.model.SpecificArrangement

@Entity(tableName = "inter_crop_recommendation")
data class InterCropRecommendationEntity(
    @PrimaryKey val id: String,
    val recommendationId: String, // Foreign key to CropRecommendationEntity
    val rank: Int,
    val intercropType: String,
    val noOfCrops: Int,
    val arrangement: String,
    val specificArrangement: List<SpecificArrangement>,
    val description: String,
    val benefits: List<String>
)

data class InterCropRecommendationWithRelations(
    @Embedded val interCropRecommendation: InterCropRecommendationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "interCropId"
    )
    val crops: List<MonoCropEntity>
)
