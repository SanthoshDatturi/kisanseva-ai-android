package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.InvestmentBreakdown
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.flow.Flow

interface InvestmentBreakdownRepository {
    fun getBreakdownById(id: String): Flow<InvestmentBreakdown?>
    fun getBreakdownByCropId(cropId: String): Flow<InvestmentBreakdown?>
    suspend fun deleteBreakdown(id: String): Result<Unit, DataError.Network>
    suspend fun refreshBreakdownById(id: String): Result<Unit, DataError.Network>
    suspend fun refreshBreakdownByCropId(cropId: String): Result<Unit, DataError.Network>
}
