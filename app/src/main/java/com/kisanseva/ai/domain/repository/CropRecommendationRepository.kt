package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.model.CropRecommendationResponse
import com.kisanseva.ai.domain.model.InterCropRecommendation
import com.kisanseva.ai.domain.model.MonoCrop
import kotlinx.coroutines.flow.Flow

interface CropRecommendationRepository {

    suspend fun getCropRecommendationByFarmId(farmId: String): CropRecommendationResponse

    suspend fun getCropRecommendationById(recommendationId: String): CropRecommendationResponse

    suspend fun getMonoCropById(monoCropId: String): MonoCrop?

    suspend fun getInterCropById(interCropId: String): InterCropRecommendation?

    suspend fun requestCropRecommendation(farmId: String)

    fun listenToCropRecommendations(): Flow<CropRecommendationResponse>

    suspend fun getLatestCropRecommendation(farmId: String): CropRecommendationResponse?

    suspend fun selectCropForCultivation(
        cropId: String,
        farmId: String,
        cropRecommendationResponseId: String
    )
}
