package com.kisanseva.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kisanseva.ai.domain.model.CultivationTask

@Entity(tableName = "cultivating_calendars")
data class CultivatingCalendarEntity(
    @PrimaryKey val id: String,
    val cropId: String,
    val tasks: List<CultivationTask>
)
