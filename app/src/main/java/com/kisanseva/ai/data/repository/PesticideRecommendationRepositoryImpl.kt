package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.local.dao.PesticideRecommendationDao
import com.kisanseva.ai.data.local.entity.PesticideRecommendationEntity
import com.kisanseva.ai.data.remote.PesticideRecommendationApi
import com.kisanseva.ai.data.remote.websocket.Actions
import com.kisanseva.ai.data.remote.websocket.WebSocketController
import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.PesticideRecommendationRequestData
import com.kisanseva.ai.domain.model.PesticideRecommendationResponse
import com.kisanseva.ai.domain.model.PesticideStage
import com.kisanseva.ai.domain.model.PesticideStageUpdateRequest
import com.kisanseva.ai.domain.repository.PesticideRecommendationRepository
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

class PesticideRecommendationRepositoryImpl(
    private val api: PesticideRecommendationApi,
    private val dao: PesticideRecommendationDao,
    private val webSocketController: WebSocketController
) : PesticideRecommendationRepository {

    override fun getRecommendationById(recommendationId: String): Flow<PesticideRecommendationResponse?> {
        return dao.getRecommendationById(recommendationId).map { it?.let { mapToDomain(it) } }
    }

    override fun getRecommendationsByCropId(cropId: String): Flow<List<PesticideRecommendationResponse>> {
        return dao.getRecommendationsByCropId(cropId).map { list -> list.map { mapToDomain(it) } }
    }

    override suspend fun refreshRecommendationsByCropId(cropId: String): Result<Unit, DataError.Network> {
        return when (val result = api.getRecommendationsByCropId(cropId)) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                result.data.forEach { dao.insertRecommendation(mapToEntity(it)) }
                Result.Success<Unit, DataError.Network>(Unit)
            }
        }
    }

    override suspend fun refreshRecommendationById(recommendationId: String): Result<Unit, DataError.Network> {
        return when (val result = api.getRecommendationById(recommendationId)) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                dao.insertRecommendation(mapToEntity(result.data))
                Result.Success<Unit, DataError.Network>(Unit)
            }
        }
    }

    override suspend fun deleteRecommendation(recommendationId: String): Result<Unit, DataError.Network> {
        return when (val result = api.deleteRecommendation(recommendationId)) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                dao.deleteRecommendation(recommendationId)
                Result.Success<Unit, DataError.Network>(Unit)
            }
        }
    }

    override suspend fun requestPesticideRecommendation(
        cropId: String,
        farmId: String,
        description: String,
        files: List<String>
    ) {
        webSocketController.sendMessage(
            Actions.PESTICIDE_RECOMMENDATION,
            PesticideRecommendationRequestData(
                cropId = cropId,
                farmId = farmId,
                pestOrDiseaseDescription = description,
                files = files
            )
        )
    }

    override fun listenToPesticideRecommendations(): Flow<PesticideRecommendationResponse> {
        return webSocketController.messages
            .filter { it.action == Actions.PESTICIDE_RECOMMENDATION }
            .mapNotNull { it.data as? PesticideRecommendationResponse }
            .onEach { recommendation ->
                dao.insertRecommendation(mapToEntity(recommendation))
            }
    }

    override suspend fun updatePesticideStage(
        recommendationId: String,
        pesticideId: String,
        stage: PesticideStage,
        appliedDate: String?
    ): Result<Unit, DataError.Network> {
        return when (val result = api.updatePesticideStage(
            recommendationId,
            PesticideStageUpdateRequest(pesticideId, stage, appliedDate)
        )) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                // Update local cache
                val local = dao.getRecommendationById(recommendationId).firstOrNull()
                if (local != null) {
                    val updatedRecommendations = local.recommendations.map {
                        if (it.id == pesticideId) {
                            it.copy(stage = stage, appliedDate = appliedDate)
                        } else {
                            it
                        }
                    }
                    dao.insertRecommendation(local.copy(recommendations = updatedRecommendations))
                }
                Result.Success<Unit, DataError.Network>(Unit)
            }
        }
    }

    private fun mapToEntity(domain: PesticideRecommendationResponse): PesticideRecommendationEntity {
        return PesticideRecommendationEntity(
            id = domain.id,
            farmId = domain.farmId,
            cropId = domain.cropId,
            timestamp = domain.timestamp,
            diseaseDetails = domain.diseaseDetails,
            recommendations = domain.recommendations,
            generalAdvice = domain.generalAdvice
        )
    }

    private fun mapToDomain(entity: PesticideRecommendationEntity): PesticideRecommendationResponse {
        return PesticideRecommendationResponse(
            id = entity.id,
            farmId = entity.farmId,
            cropId = entity.cropId,
            timestamp = entity.timestamp,
            diseaseDetails = entity.diseaseDetails,
            recommendations = entity.recommendations,
            generalAdvice = entity.generalAdvice
        )
    }
}
