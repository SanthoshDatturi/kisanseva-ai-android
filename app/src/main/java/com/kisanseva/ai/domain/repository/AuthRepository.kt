package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.OTPSendRequest
import com.kisanseva.ai.domain.model.OTPStatusResponse
import com.kisanseva.ai.domain.model.OTPVerifyRequest
import com.kisanseva.ai.domain.model.Token
import com.kisanseva.ai.domain.state.Result

interface AuthRepository {
    suspend fun sendOtp(request: OTPSendRequest): Result<OTPStatusResponse, DataError.Network>
    suspend fun verifyOtp(request: OTPVerifyRequest): Result<Token, DataError.Network>
    suspend fun getToken(): String?
    suspend fun clearToken()
}
