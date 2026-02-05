package com.kisanseva.ai.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kisanseva.ai.domain.model.FinancialForecasting
import com.kisanseva.ai.domain.model.RiskFactor
import com.kisanseva.ai.domain.model.SowingWindow

@Entity(tableName = "mono_crop")
data class MonoCropEntity(
    @PrimaryKey val id: String,
    val recommendationId: String, // Foreign key to CropRecommendationEntity
    val interCropId: String?, // Foreign key to InterCropRecommendationEntity
    val rank: Int?,
    val cropName: String,
    val variety: String,
    val imageUrl: String,
    val suitabilityScore: Double,
    val confidence: Double,
    val expectedYieldPerAcre: String,
    @Embedded val sowingWindow: SowingWindow,
    val growingPeriodDays: Int,
    @Embedded val financialForecasting: FinancialForecasting,
    val reasons: List<String>,
    val riskFactors: List<RiskFactor>,
    val description: String
)
