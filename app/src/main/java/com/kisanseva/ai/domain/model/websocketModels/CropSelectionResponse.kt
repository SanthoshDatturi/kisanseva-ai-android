package com.kisanseva.ai.domain.model.websocketModels

import com.kisanseva.ai.domain.model.CultivationCalendar
import com.kisanseva.ai.domain.model.InvestmentBreakdown
import com.kisanseva.ai.domain.model.SoilHealthRecommendations
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CropSelectionResponse(
    @SerialName("cultivation_calendar")
    val cultivationCalendar: List<CultivationCalendar>,
    @SerialName("investment_breakdown")
    val investmentBreakdown: List<InvestmentBreakdown>,
    @SerialName("soil_health_recommendations")
    val soilHealthRecommendations: SoilHealthRecommendations
)