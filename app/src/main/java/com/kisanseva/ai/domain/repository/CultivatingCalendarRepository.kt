package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.model.CultivationCalendar

interface CultivatingCalendarRepository {
    suspend fun getCalendarById(calendarId: String): CultivationCalendar
    suspend fun getCalendarByCropId(cropId: String): CultivationCalendar
    suspend fun deleteCalendar(calendarId: String)
}
