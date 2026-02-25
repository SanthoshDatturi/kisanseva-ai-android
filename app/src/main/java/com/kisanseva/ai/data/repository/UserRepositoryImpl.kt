package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.local.dao.UserDao
import com.kisanseva.ai.data.local.entity.toDomain
import com.kisanseva.ai.data.local.entity.toEntity
import com.kisanseva.ai.data.remote.UserApi
import com.kisanseva.ai.domain.model.User
import com.kisanseva.ai.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    private val userApi: UserApi,
    private val userDao: UserDao
) : UserRepository {

    override fun getUser(): Flow<User?> {
        return userDao.getUser().map { it?.toDomain() }
    }

    override suspend fun refreshUser() {
        val user = userApi.getProfile()
        userDao.insertUser(user.toEntity())
    }

    override suspend fun clearUser() {
        userDao.clearUser()
    }
}
