package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.model.OTPSendRequest
import com.kisanseva.ai.domain.model.OTPStatusResponse
import com.kisanseva.ai.domain.model.OTPVerifyRequest
import com.kisanseva.ai.domain.model.Token

interface AuthRepository {
    suspend fun sendOtp(request: OTPSendRequest): OTPStatusResponse
    suspend fun verifyOtp(request: OTPVerifyRequest): Token
    suspend fun getToken(): String?
    suspend fun clearToken()
}
