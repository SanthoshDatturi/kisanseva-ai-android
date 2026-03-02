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
import com.kisanseva.ai.domain.model.websocketModels.WorkflowChunkEnvelope
import com.kisanseva.ai.domain.model.websocketModels.WorkflowEvents
import com.kisanseva.ai.domain.repository.PesticideRecommendationRepository
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive

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

    override fun listenToPesticideRecommendationProgress(): Flow<String> {
        return webSocketController.workflowEvents
            .filter { it.action == Actions.PESTICIDE_RECOMMENDATION }
            .mapNotNull { event ->
                when (event.event) {
                    WorkflowEvents.STEP_STARTED -> event.step?.let { "Running ${it.replace("_", " ")}..." }
                    WorkflowEvents.WORKFLOW_FAILED -> event.data.extractErrorMessage()?.let { "Failed: $it" }
                    WorkflowEvents.CHUNK -> event.data.toChunkMessage()
                    else -> null
                }
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

    private fun JsonElement?.toChunkMessage(): String? {
        if (this == null) return null
        val envelope = runCatching {
            webSocketController.json.decodeFromJsonElement<WorkflowChunkEnvelope>(this)
        }.getOrNull() ?: return null
        val payload = envelope.data as? JsonObject

        return when (envelope.chunkType) {
            "media_ready" -> {
                val count = payload?.get("file_count")?.jsonPrimitive?.contentOrNull
                if (count != null) "Processed $count input files." else "Processed input files."
            }
            "diagnostic_ready" -> {
                val likelyIssue = payload?.get("likely_issue")?.jsonPrimitive?.contentOrNull
                if (!likelyIssue.isNullOrBlank()) {
                    "Diagnosis ready: $likelyIssue"
                } else {
                    "Diagnosis prepared."
                }
            }
            "pesticide_item_ready" -> {
                val name = payload?.get("pesticide_name")?.jsonPrimitive?.contentOrNull
                val type = payload?.get("pesticide_type")?.jsonPrimitive?.contentOrNull
                when {
                    !name.isNullOrBlank() && !type.isNullOrBlank() -> "Recommendation ready: $name ($type)"
                    !name.isNullOrBlank() -> "Recommendation ready: $name"
                    else -> "Pesticide recommendation item prepared."
                }
            }
            else -> null
        }
    }

    private fun JsonElement?.extractErrorMessage(): String? {
        val obj = this as? JsonObject ?: return null
        return obj["error"]?.jsonPrimitive?.contentOrNull
    }
}
