package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.local.DataStoreManager
import com.kisanseva.ai.data.local.dao.UserDao
import com.kisanseva.ai.data.local.entity.toEntity
import com.kisanseva.ai.data.remote.AuthApi
import com.kisanseva.ai.domain.repository.AuthRepository
import com.kisanseva.ai.domain.model.OTPSendRequest
import com.kisanseva.ai.domain.model.OTPStatusResponse
import com.kisanseva.ai.domain.model.OTPVerifyRequest
import com.kisanseva.ai.domain.model.Token
import kotlinx.coroutines.flow.first

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val dataStore: DataStoreManager,
    private val userDao: UserDao
) : AuthRepository {

    override suspend fun sendOtp(request: OTPSendRequest): OTPStatusResponse {
        return api.sendOtp(request)
    }

    override suspend fun verifyOtp(request: OTPVerifyRequest): Token {
        val token = api.verifyOtp(request)
        dataStore.saveToken(token.accessToken)
        userDao.insertUser(token.user.toEntity())
        return token
    }

    override suspend fun getToken(): String? = dataStore.token.first()

    override suspend fun clearToken() = dataStore.clearToken()
}
