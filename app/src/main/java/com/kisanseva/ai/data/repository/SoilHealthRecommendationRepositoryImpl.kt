package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.local.dao.SoilHealthRecommendationDao
import com.kisanseva.ai.data.local.entity.SoilHealthRecommendationEntity
import com.kisanseva.ai.data.remote.SoilHealthRecommendationApi
import com.kisanseva.ai.domain.model.SoilHealthRecommendations
import com.kisanseva.ai.domain.repository.SoilHealthRecommendationRepository

class SoilHealthRecommendationRepositoryImpl(
    private val api: SoilHealthRecommendationApi,
    private val dao: SoilHealthRecommendationDao
) : SoilHealthRecommendationRepository {

    override suspend fun getRecommendationById(id: String): SoilHealthRecommendations {
        val local = dao.getRecommendationById(id)
        return if (local != null) {
            entityToDomain(local)
        } else {
            val remote = api.getRecommendationById(id)
            dao.insertRecommendation(domainToEntity(remote))
            remote
        }
    }

    override suspend fun getRecommendationsByCropId(cropId: String): List<SoilHealthRecommendations> {
        val local = dao.getRecommendationsByCropId(cropId)
        return if (local.isNotEmpty()) {
            local.map { entityToDomain(it) }
        } else {
            val remote = api.getRecommendationsByCropId(cropId)
            remote.forEach { dao.insertRecommendation(domainToEntity(it)) }
            remote
        }
    }

    override suspend fun deleteRecommendation(id: String) {
        api.deleteRecommendation(id)
        dao.deleteRecommendationById(id)
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
