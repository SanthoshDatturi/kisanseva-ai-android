package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.local.dao.UserDao
import com.kisanseva.ai.data.local.entity.toDomain
import com.kisanseva.ai.data.local.entity.toEntity
import com.kisanseva.ai.data.remote.UserApi
import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.User
import com.kisanseva.ai.domain.repository.UserRepository
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    private val userApi: UserApi,
    private val userDao: UserDao
) : UserRepository {

    override fun getUser(): Flow<User?> {
        return userDao.getUser().map { it?.toDomain() }
    }

    override suspend fun refreshUser(): Result<Unit, DataError.Network> {
        return when (val result = userApi.getProfile()) {
            is Result.Error -> Result.Error(result.error)
            is Result.Success -> {
                userDao.insertUser(result.data.toEntity())
                Result.Success(Unit)
            }
        }
    }

    override suspend fun clearUser() {
        userDao.clearUser()
    }
}
