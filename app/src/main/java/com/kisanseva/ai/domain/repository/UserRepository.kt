package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.User
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(): Flow<User?>
    suspend fun refreshUser(): Result<Unit, DataError.Network>
    suspend fun clearUser()
}
