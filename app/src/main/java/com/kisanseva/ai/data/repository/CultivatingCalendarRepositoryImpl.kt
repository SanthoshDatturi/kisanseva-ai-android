package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.local.dao.CultivatingCalendarDao
import com.kisanseva.ai.data.local.entity.CultivatingCalendarEntity
import com.kisanseva.ai.data.remote.CultivatingCalendarApi
import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.CultivationCalendar
import com.kisanseva.ai.domain.repository.CultivatingCalendarRepository
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CultivatingCalendarRepositoryImpl(
    private val calendarApi: CultivatingCalendarApi,
    private val calendarDao: CultivatingCalendarDao
) : CultivatingCalendarRepository {

    override fun getCalendarById(calendarId: String): Flow<CultivationCalendar?> {
        return calendarDao.getCalendarById(calendarId).map { entity ->
            entity?.let { entityToDomain(it) }
        }
    }

    override fun getCalendarByCropId(cropId: String): Flow<CultivationCalendar?> {
        return calendarDao.getCalendarByCropId(cropId).map { entity ->
            entity?.let { entityToDomain(it) }
        }
    }

    override suspend fun refreshCalendarById(calendarId: String): Result<Unit, DataError.Network> {
        return when (val result = calendarApi.getCalendarById(calendarId)) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                calendarDao.insertCalendar(domainToEntity(result.data))
                Result.Success<Unit, DataError.Network>(Unit)
            }
        }
    }

    override suspend fun refreshCalendarByCropId(cropId: String): Result<Unit, DataError.Network> {
        return when (val result = calendarApi.getCalendarByCropId(cropId)) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                calendarDao.insertCalendar(domainToEntity(result.data))
                Result.Success<Unit, DataError.Network>(Unit)
            }
        }
    }

    override suspend fun deleteCalendar(calendarId: String): Result<Unit, DataError.Network> {
        return when (val result = calendarApi.deleteCalendar(calendarId)) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                calendarDao.deleteCalendarById(calendarId)
                Result.Success<Unit, DataError.Network>(Unit)
            }
        }
    }

    private fun domainToEntity(calendar: CultivationCalendar): CultivatingCalendarEntity {
        return CultivatingCalendarEntity(
            id = calendar.id,
            cropId = calendar.cropId,
            tasks = calendar.tasks
        )
    }

    private fun entityToDomain(entity: CultivatingCalendarEntity): CultivationCalendar {
        return CultivationCalendar(
            id = entity.id,
            cropId = entity.cropId,
            tasks = entity.tasks
        )
    }
}
