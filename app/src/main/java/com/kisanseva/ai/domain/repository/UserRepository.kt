package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.model.User

interface UserRepository {
    suspend fun getProfile(): User
}
