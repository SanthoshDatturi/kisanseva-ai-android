package com.kisanseva.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OTPSendRequest(
    val phone: String,
    val name: String? = null,
    val language: String? = null
)

@Serializable
data class OTPVerifyRequest(
    val phone: String,
    val otp: String
)

@Serializable
data class OTPStatusResponse(
    val message: String,
    val phone: String? = null
)

@Serializable
data class Token(
    @SerialName("access_token")
    val accessToken: String,
    val user: User
)
