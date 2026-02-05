package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.local.dao.CultivatingCalendarDao
import com.kisanseva.ai.data.local.entity.CultivatingCalendarEntity
import com.kisanseva.ai.data.remote.CultivatingCalendarApi
import com.kisanseva.ai.domain.model.CultivationCalendar
import com.kisanseva.ai.domain.repository.CultivatingCalendarRepository

class CultivatingCalendarRepositoryImpl(
    private val calendarApi: CultivatingCalendarApi,
    private val calendarDao: CultivatingCalendarDao
) : CultivatingCalendarRepository {

    override suspend fun getCalendarById(calendarId: String): CultivationCalendar {
        val localCalendar = calendarDao.getCalendarById(calendarId)
        return if (localCalendar != null) {
            entityToDomain(localCalendar)
        } else {
            val remoteCalendar = calendarApi.getCalendarById(calendarId)
            calendarDao.insertCalendar(domainToEntity(remoteCalendar))
            remoteCalendar
        }
    }

    override suspend fun getCalendarByCropId(cropId: String): CultivationCalendar {
        val localCalendar = calendarDao.getCalendarByCropId(cropId)
        return if (localCalendar != null) {
            entityToDomain(localCalendar)
        } else {
            val remoteCalendar = calendarApi.getCalendarByCropId(cropId)
            calendarDao.insertCalendar(domainToEntity(remoteCalendar))
            remoteCalendar
        }
    }

    override suspend fun deleteCalendar(calendarId: String) {
        calendarApi.deleteCalendar(calendarId)
        calendarDao.deleteCalendarById(calendarId)
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
