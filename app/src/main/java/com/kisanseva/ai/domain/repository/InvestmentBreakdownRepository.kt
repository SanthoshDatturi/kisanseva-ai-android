package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.model.InvestmentBreakdown

interface InvestmentBreakdownRepository {
    suspend fun getBreakdownById(id: String): InvestmentBreakdown
    suspend fun getBreakdownByCropId(cropId: String): InvestmentBreakdown
    suspend fun deleteBreakdown(id: String)
}
