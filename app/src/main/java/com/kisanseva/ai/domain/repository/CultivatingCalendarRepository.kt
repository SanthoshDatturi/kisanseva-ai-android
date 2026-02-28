package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.CultivationCalendar
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.flow.Flow

interface CultivatingCalendarRepository {
    fun getCalendarById(calendarId: String): Flow<CultivationCalendar?>
    fun getCalendarByCropId(cropId: String): Flow<CultivationCalendar?>
    suspend fun deleteCalendar(calendarId: String): Result<Unit, DataError.Network>
    suspend fun refreshCalendarById(calendarId: String): Result<Unit, DataError.Network>
    suspend fun refreshCalendarByCropId(cropId: String): Result<Unit, DataError.Network>
}
