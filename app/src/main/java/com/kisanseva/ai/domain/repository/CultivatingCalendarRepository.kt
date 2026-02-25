package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.model.CultivationCalendar
import kotlinx.coroutines.flow.Flow

interface CultivatingCalendarRepository {
    fun getCalendarById(calendarId: String): Flow<CultivationCalendar?>
    fun getCalendarByCropId(cropId: String): Flow<CultivationCalendar?>
    suspend fun deleteCalendar(calendarId: String)
    suspend fun refreshCalendarById(calendarId: String)
    suspend fun refreshCalendarByCropId(cropId: String)
}
