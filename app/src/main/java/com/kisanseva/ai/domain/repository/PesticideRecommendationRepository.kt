package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.PesticideRecommendationResponse
import com.kisanseva.ai.domain.model.PesticideStage
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.flow.Flow

interface PesticideRecommendationRepository {
    fun getRecommendationById(recommendationId: String): Flow<PesticideRecommendationResponse?>
    fun getRecommendationsByCropId(cropId: String): Flow<List<PesticideRecommendationResponse>>
    suspend fun deleteRecommendation(recommendationId: String): Result<Unit, DataError.Network>
    suspend fun requestPesticideRecommendation(cropId: String, farmId: String, description: String, files: List<String>)
    fun listenToPesticideRecommendations(): Flow<PesticideRecommendationResponse>
    suspend fun updatePesticideStage(recommendationId: String, pesticideId: String, stage: PesticideStage, appliedDate: String? = null): Result<Unit, DataError.Network>
    suspend fun refreshRecommendationsByCropId(cropId: String): Result<Unit, DataError.Network>
    suspend fun refreshRecommendationById(recommendationId: String): Result<Unit, DataError.Network>
}
