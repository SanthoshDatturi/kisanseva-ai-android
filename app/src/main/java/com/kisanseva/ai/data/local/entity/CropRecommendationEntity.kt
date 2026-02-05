package com.kisanseva.ai.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.kisanseva.ai.domain.model.RecommendationStatus

@Entity(tableName = "crop_recommendation")
data class CropRecommendationEntity(
    @PrimaryKey val id: String,
    val farmId: String?,
    val timestamp: String,
    val status: RecommendationStatus,
)

data class CropRecommendationWithRelations(
    @Embedded val cropRecommendation: CropRecommendationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "recommendationId"
    )
    val allMonoCrops: List<MonoCropEntity>,
    @Relation(
        entity = InterCropRecommendationEntity::class,
        parentColumn = "id",
        entityColumn = "recommendationId"
    )
    val interCrops: List<InterCropRecommendationWithRelations>
)
