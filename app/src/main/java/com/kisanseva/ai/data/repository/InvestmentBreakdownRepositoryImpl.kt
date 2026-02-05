package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.local.dao.InvestmentBreakdownDao
import com.kisanseva.ai.data.local.entity.InvestmentBreakdownEntity
import com.kisanseva.ai.data.remote.InvestmentBreakdownApi
import com.kisanseva.ai.domain.model.InvestmentBreakdown
import com.kisanseva.ai.domain.repository.InvestmentBreakdownRepository

class InvestmentBreakdownRepositoryImpl(
    private val api: InvestmentBreakdownApi,
    private val dao: InvestmentBreakdownDao
) : InvestmentBreakdownRepository {

    override suspend fun getBreakdownById(id: String): InvestmentBreakdown {
        val local = dao.getBreakdownById(id)
        return if (local != null) {
            entityToDomain(local)
        } else {
            val remote = api.getBreakdownById(id)
            dao.insertBreakdown(domainToEntity(remote))
            remote
        }
    }

    override suspend fun getBreakdownByCropId(cropId: String): InvestmentBreakdown {
        val local = dao.getBreakdownByCropId(cropId)
        return if (local != null) {
            entityToDomain(local)
        } else {
            val remote = api.getBreakdownByCropId(cropId)
            dao.insertBreakdown(domainToEntity(remote))
            remote
        }
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
