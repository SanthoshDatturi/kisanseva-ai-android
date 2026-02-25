package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.local.dao.InvestmentBreakdownDao
import com.kisanseva.ai.data.local.entity.InvestmentBreakdownEntity
import com.kisanseva.ai.data.remote.InvestmentBreakdownApi
import com.kisanseva.ai.domain.model.InvestmentBreakdown
import com.kisanseva.ai.domain.repository.InvestmentBreakdownRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InvestmentBreakdownRepositoryImpl(
    private val api: InvestmentBreakdownApi,
    private val dao: InvestmentBreakdownDao
) : InvestmentBreakdownRepository {

    override fun getBreakdownById(id: String): Flow<InvestmentBreakdown?> {
        return dao.getBreakdownById(id).map { it?.let { entityToDomain(it) } }
    }

    override fun getBreakdownByCropId(cropId: String): Flow<InvestmentBreakdown?> {
        return dao.getBreakdownByCropId(cropId).map { it?.let { entityToDomain(it) } }
    }

    override suspend fun refreshBreakdownById(id: String) {
        val remote = api.getBreakdownById(id)
        dao.insertBreakdown(domainToEntity(remote))
    }

    override suspend fun refreshBreakdownByCropId(cropId: String) {
        val remote = api.getBreakdownByCropId(cropId)
        dao.insertBreakdown(domainToEntity(remote))
    }

    override suspend fun deleteBreakdown(id: String) {
        api.deleteBreakdown(id)
        dao.deleteBreakdownById(id)
    }

    private fun domainToEntity(domain: InvestmentBreakdown): InvestmentBreakdownEntity {
        return InvestmentBreakdownEntity(
            id = domain.id,
            cropId = domain.cropId,
            investments = domain.investments,
            profitability = domain.profitability
        )
    }

    private fun entityToDomain(entity: InvestmentBreakdownEntity): InvestmentBreakdown {
        return InvestmentBreakdown(
            id = entity.id,
            cropId = entity.cropId,
            investments = entity.investments,
            profitability = entity.profitability
        )
    }
}
