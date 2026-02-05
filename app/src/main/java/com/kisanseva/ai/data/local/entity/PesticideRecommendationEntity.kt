package com.kisanseva.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kisanseva.ai.domain.model.PesticideInfo

@Entity(tableName = "pesticide_recommendation")
data class PesticideRecommendationEntity(
    @PrimaryKey
    val id: String,
    val farmId: String?,
    val cropId: String?,
    val timestamp: Double,
    val diseaseDetails: String,
    val recommendations: List<PesticideInfo>,
    val generalAdvice: String
)
