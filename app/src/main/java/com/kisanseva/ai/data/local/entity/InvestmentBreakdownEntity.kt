package com.kisanseva.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kisanseva.ai.domain.model.Investment
import com.kisanseva.ai.domain.model.Profitability

@Entity(tableName = "investment_breakdowns")
data class InvestmentBreakdownEntity(
    @PrimaryKey val id: String,
    val cropId: String,
    val investments: List<Investment>,
    val profitability: Profitability
)
