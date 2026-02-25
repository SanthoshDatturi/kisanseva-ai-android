package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getProfile(): User
    fun getUser(): Flow<User?>
    suspend fun clearUser()
}
