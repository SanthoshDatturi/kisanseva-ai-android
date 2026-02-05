package com.kisanseva.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    @SerialName("is_verified")
    val isVerified: Boolean,
    val language: String,
    val name: String,
    val phone: String,
    val role: String
)