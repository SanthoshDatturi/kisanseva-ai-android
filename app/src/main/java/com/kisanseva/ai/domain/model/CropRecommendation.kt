package com.kisanseva.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SowingWindow(
    @SerialName("start_date")
    val startDate: String,
    @SerialName("end_date")
    val endDate: String,
    @SerialName("optimal_date")
    val optimalDate: String
)

@Serializable
data class FinancialForecasting(
    @SerialName("total_estimated_investment")
    val totalEstimatedInvestment: String,
    @SerialName("market_price_current")
    val marketPriceCurrent: String,
    @SerialName("price_trend")
    val priceTrend: String,
    @SerialName("total_revenue_estimate")
    val totalRevenueEstimate: String
)

@Serializable
enum class RiskImpact {
    @SerialName("low")
    LOW,
    @SerialName("medium")
    MEDIUM,
    @SerialName("high")
    HIGH
}

@Serializable
data class RiskFactor(
    val risk: String,
    val probability: Double,
    val impact: RiskImpact,
    val mitigation: String
)

@Serializable
data class MonoCrop(
    @SerialName("_id")
    val id: String,
    val rank: Int? = null,
    @SerialName("crop_name")
    val cropName: String,
    val variety: String,
    @SerialName("image_url")
    val imageUrl: String,
    @SerialName("suitability_score")
    val suitabilityScore: Double,
    val confidence: Double,
    @SerialName("expected_yield_per_acre")
    val expectedYieldPerAcre: String,
    @SerialName("sowing_window")
    val sowingWindow: SowingWindow,
    @SerialName("growing_period_days")
    val growingPeriodDays: Int,
    @SerialName("financial_forecasting")
    val financialForecasting: FinancialForecasting,
    val reasons: List<String>,
    @SerialName("risk_factors")
    val riskFactors: List<RiskFactor>,
    val description: String
)


@Serializable
data class InterCropRecommendation(
    @SerialName("_id")
    val id: String,
    val rank: Int,
    @SerialName("intercrop_type")
    val intercropType: String,
    @SerialName("no_of_crops")
    val noOfCrops: Int,
    val arrangement: String,
    @SerialName("specific_arrangement")
    val specificArrangement: List<SpecificArrangement>,
    val crops: List<MonoCrop>,
    val description: String,
    val benefits: List<String> = listOf()
)

@Serializable
enum class RecommendationStatus {
    @SerialName("success")
    SUCCESS,
    @SerialName("failure")
    FAILURE,
    @SerialName("pending")
    PENDING
}


@Serializable
data class CropRecommendationResponse(
    @SerialName("_id")
    val id: String,
    @SerialName("farm_id")
    val farmId: String? = null,
    val timestamp: String,
    val status: RecommendationStatus,
    @SerialName("mono_crops")
    val monoCrops: List<MonoCrop>,
    @SerialName("inter_crops")
    val interCrops: List<InterCropRecommendation>
)

@Serializable
data class CropRecommendationRequestData(
    @SerialName("farm_id") val farmId: String
)

@Serializable
data class SelectCropRequestData(
    @SerialName("selected_crop_id") val selectedCropId: String,
    @SerialName("farm_id") val farmId: String,
    @SerialName("crop_recommendation_response_id") val cropRecommendationResponseId: String
)
