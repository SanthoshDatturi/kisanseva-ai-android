package com.kisanseva.ai.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kisanseva.ai.domain.model.CropState

@Entity(tableName = "cultivating_crop")
data class CultivatingCropEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo("farm_id") val farmId: String,
    val name: String,
    val variety: String,
    @ColumnInfo("image_url") val imageUrl: String,
    @ColumnInfo("crop_state") val cropState: CropState,
    val description: String,
    @ColumnInfo("intercropping_id") val intercroppingId: String? = null
)
