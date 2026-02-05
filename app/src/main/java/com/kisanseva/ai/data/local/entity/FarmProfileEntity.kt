package com.kisanseva.ai.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kisanseva.ai.domain.model.IrrigationSystem
import com.kisanseva.ai.domain.model.Location
import com.kisanseva.ai.domain.model.PreviousCrops
import com.kisanseva.ai.domain.model.SoilTestProperties
import com.kisanseva.ai.domain.model.SoilType
import com.kisanseva.ai.domain.model.WaterSource

@Entity(tableName = "farm_profiles")
data class FarmProfileEntity(
    @PrimaryKey
    val id: String,
    val farmerId: String,
    val name: String,
    @Embedded val location: Location,
    val soilType: SoilType,
    val crops: List<PreviousCrops>? = null,
    val totalAreaAcres: Double,
    val cultivatedAreaAcres: Double,
    @Embedded val soilTestProperties: SoilTestProperties? = null,
    val waterSource: WaterSource,
    val irrigationSystem: IrrigationSystem? = null
)
