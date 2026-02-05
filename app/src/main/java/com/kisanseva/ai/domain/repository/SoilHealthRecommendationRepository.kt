package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.model.SoilHealthRecommendations

interface SoilHealthRecommendationRepository {
    suspend fun getRecommendationById(id: String): SoilHealthRecommendations
    suspend fun getRecommendationsByCropId(cropId: String): List<SoilHealthRecommendations>
    suspend fun deleteRecommendation(id: String)
}
