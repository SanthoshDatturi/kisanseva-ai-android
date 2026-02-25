package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.model.SoilHealthRecommendations
import kotlinx.coroutines.flow.Flow

interface SoilHealthRecommendationRepository {
    fun getRecommendationById(id: String): Flow<SoilHealthRecommendations?>
    fun getRecommendationsByCropId(cropId: String): Flow<List<SoilHealthRecommendations>>
    suspend fun deleteRecommendation(id: String)
    suspend fun refreshRecommendationById(id: String)
    suspend fun refreshRecommendationsByCropId(cropId: String)
}
