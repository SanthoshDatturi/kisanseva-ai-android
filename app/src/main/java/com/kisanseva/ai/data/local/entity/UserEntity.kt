package com.kisanseva.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kisanseva.ai.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val isVerified: Boolean,
    val language: String,
    val name: String,
    val phone: String,
    val role: String
)

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        isVerified = isVerified,
        language = language,
        name = name,
        phone = phone,
        role = role
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        isVerified = isVerified,
        language = language,
        name = name,
        phone = phone,
        role = role
    )
}
