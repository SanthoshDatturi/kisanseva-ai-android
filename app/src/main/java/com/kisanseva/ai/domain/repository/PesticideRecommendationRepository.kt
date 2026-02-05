package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.model.PesticideRecommendationResponse
import com.kisanseva.ai.domain.model.PesticideStage
import kotlinx.coroutines.flow.Flow

interface PesticideRecommendationRepository {
    suspend fun getRecommendationById(recommendationId: String): PesticideRecommendationResponse
    suspend fun getRecommendationsByCropId(cropId: String): List<PesticideRecommendationResponse>
    suspend fun deleteRecommendation(recommendationId: String)
    suspend fun requestPesticideRecommendation(cropId: String, farmId: String, description: String, files: List<String>)
    fun listenToPesticideRecommendations(): Flow<PesticideRecommendationResponse>
    suspend fun updatePesticideStage(recommendationId: String, pesticideId: String, stage: PesticideStage, appliedDate: String? = null)
}
