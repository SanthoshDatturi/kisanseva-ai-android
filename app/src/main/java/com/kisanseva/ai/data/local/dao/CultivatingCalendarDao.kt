package com.kisanseva.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanseva.ai.data.local.entity.CultivatingCalendarEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CultivatingCalendarDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendar(calendar: CultivatingCalendarEntity)

    @Query("SELECT * FROM cultivating_calendars WHERE id = :id")
    fun getCalendarById(id: String): Flow<CultivatingCalendarEntity?>

    @Query("SELECT * FROM cultivating_calendars WHERE cropId = :cropId")
    fun getCalendarByCropId(cropId: String): Flow<CultivatingCalendarEntity?>

    @Query("DELETE FROM cultivating_calendars WHERE id = :id")
    suspend fun deleteCalendarById(id: String)
}
