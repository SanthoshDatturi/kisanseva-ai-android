package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.local.DataStoreManager
import com.kisanseva.ai.data.local.dao.UserDao
import com.kisanseva.ai.data.local.entity.toEntity
import com.kisanseva.ai.data.remote.AuthApi
import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.OTPSendRequest
import com.kisanseva.ai.domain.model.OTPStatusResponse
import com.kisanseva.ai.domain.model.OTPVerifyRequest
import com.kisanseva.ai.domain.model.Token
import com.kisanseva.ai.domain.repository.AuthRepository
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.flow.first

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val dataStore: DataStoreManager,
    private val userDao: UserDao
) : AuthRepository {

    override suspend fun sendOtp(request: OTPSendRequest): Result<OTPStatusResponse, DataError.Network> {
        return api.sendOtp(request)
    }

    override suspend fun verifyOtp(request: OTPVerifyRequest): Result<Token, DataError.Network> {
        return when (val result = api.verifyOtp(request)) {
            is Result.Error -> Result.Error(result.error)
            is Result.Success -> {
                dataStore.saveToken(result.data.accessToken)
                userDao.insertUser(result.data.user.toEntity())
                Result.Success(result.data)
            }
        }
    }

    override suspend fun getToken(): String? = dataStore.token.first()

    override suspend fun clearToken() = dataStore.clearToken()
}
