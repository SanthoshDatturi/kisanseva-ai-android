package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.SoilHealthRecommendations
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.flow.Flow

interface SoilHealthRecommendationRepository {
    fun getRecommendationById(id: String): Flow<SoilHealthRecommendations?>
    fun getRecommendationsByCropId(cropId: String): Flow<List<SoilHealthRecommendations>>
    suspend fun deleteRecommendation(id: String): Result<Unit, DataError.Network>
    suspend fun refreshRecommendationById(id: String): Result<Unit, DataError.Network>
    suspend fun refreshRecommendationsByCropId(cropId: String): Result<Unit, DataError.Network>
}
