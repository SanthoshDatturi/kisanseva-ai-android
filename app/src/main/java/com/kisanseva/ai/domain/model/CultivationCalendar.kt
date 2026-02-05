package com.kisanseva.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TaskState {
    @SerialName("pending")
    PENDING,
    @SerialName("completed")
    COMPLETED,
    @SerialName("canceled")
    CANCELED
}

@Serializable
enum class Priority {
    @SerialName("low")
    LOW,
    @SerialName("medium")
    MEDIUM,
    @SerialName("high")
    HIGH,
    @SerialName("critical")
    CRITICAL
}

@Serializable
data class CultivationTask(
    val task: String,
    @SerialName("from_date") val fromDate: String,
    @SerialName("to_date") val toDate: String,
    val state: TaskState = TaskState.PENDING,
    val priority: Priority
)

@Serializable
data class CultivationCalendar(
    val id: String,
    @SerialName("crop_id") val cropId: String,
    val tasks: List<CultivationTask>
)
