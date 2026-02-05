package com.kisanseva.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kisanseva.ai.domain.model.ImmediateAction

@Entity(tableName = "soil_health_recommendations")
data class SoilHealthRecommendationEntity(
    @PrimaryKey val id: String,
    val cropId: String,
    val immediateActions: List<ImmediateAction>,
    val description: String,
    val longTermImprovements: List<String>
)
