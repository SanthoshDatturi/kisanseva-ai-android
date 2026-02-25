package com.kisanseva.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Investment(
    val reason: String,
    val amount: Double
)

@Serializable
data class Profitability(
    @SerialName("gross_income") val grossIncome: Double,
    @SerialName("total_cost") val totalCost: Double,
    @SerialName("net_profit") val netProfit: Double,
    @SerialName("roi_percentage") val roiPercentage: Double,
    @SerialName("break_even_yield") val breakEvenYield: String
)

@Serializable
data class InvestmentBreakdown(
    @SerialName("_id")
    val id: String,
    @SerialName("crop_id") val cropId: String,
    val investments: List<Investment>,
    val profitability: Profitability
)
