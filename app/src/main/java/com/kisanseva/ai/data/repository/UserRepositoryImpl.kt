package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.remote.UserApi
import com.kisanseva.ai.domain.model.User
import com.kisanseva.ai.domain.repository.UserRepository

class UserRepositoryImpl(
    private val userApi: UserApi
) : UserRepository {
    override suspend fun getProfile(): User {
        return userApi.getProfile()
    }
}
