package com.kisanseva.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanseva.ai.data.local.entity.CultivatingCalendarEntity

@Dao
interface CultivatingCalendarDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendar(calendar: CultivatingCalendarEntity)

    @Query("SELECT * FROM cultivating_calendars WHERE id = :id")
    suspend fun getCalendarById(id: String): CultivatingCalendarEntity?

    @Query("SELECT * FROM cultivating_calendars WHERE cropId = :cropId")
    suspend fun getCalendarByCropId(cropId: String): CultivatingCalendarEntity?

    @Query("DELETE FROM cultivating_calendars WHERE id = :id")
    suspend fun deleteCalendarById(id: String)
}
