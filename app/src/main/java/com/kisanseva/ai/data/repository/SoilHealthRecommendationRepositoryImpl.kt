package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.local.dao.SoilHealthRecommendationDao
import com.kisanseva.ai.data.local.entity.SoilHealthRecommendationEntity
import com.kisanseva.ai.data.remote.SoilHealthRecommendationApi
import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.SoilHealthRecommendations
import com.kisanseva.ai.domain.repository.SoilHealthRecommendationRepository
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SoilHealthRecommendationRepositoryImpl(
    private val api: SoilHealthRecommendationApi,
    private val dao: SoilHealthRecommendationDao
) : SoilHealthRecommendationRepository {

    override fun getRecommendationById(id: String): Flow<SoilHealthRecommendations?> {
        return dao.getRecommendationById(id).map { it?.let { entityToDomain(it) } }
    }

    override fun getRecommendationsByCropId(cropId: String): Flow<List<SoilHealthRecommendations>> {
        return dao.getRecommendationsByCropId(cropId).map { list -> list.map { entityToDomain(it) } }
    }

    override suspend fun refreshRecommendationById(id: String): Result<Unit, DataError.Network> {
        return when (val result = api.getRecommendationById(id)) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                dao.insertRecommendation(domainToEntity(result.data))
                Result.Success<Unit, DataError.Network>(Unit)
            }
        }
    }

    override suspend fun refreshRecommendationsByCropId(cropId: String): Result<Unit, DataError.Network> {
        return when (val result = api.getRecommendationsByCropId(cropId)) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                result.data.forEach { dao.insertRecommendation(domainToEntity(it)) }
                Result.Success<Unit, DataError.Network>(Unit)
            }
        }
    }

    override suspend fun deleteRecommendation(id: String): Result<Unit, DataError.Network> {
        return when (val result = api.deleteRecommendation(id)) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                dao.deleteRecommendationById(id)
                Result.Success<Unit, DataError.Network>(Unit)
            }
        }
    }

    private fun domainToEntity(domain: SoilHealthRecommendations): SoilHealthRecommendationEntity {
        return SoilHealthRecommendationEntity(
            id = domain.id,
            cropId = domain.cropId,
            immediateActions = domain.immediateActions,
            description = domain.description,
            longTermImprovements = domain.longTermImprovements
        )
    }

    private fun entityToDomain(entity: SoilHealthRecommendationEntity): SoilHealthRecommendations {
        return SoilHealthRecommendations(
            id = entity.id,
            cropId = entity.cropId,
            immediateActions = entity.immediateActions,
            description = entity.description,
            longTermImprovements = entity.longTermImprovements
        )
    }
}
