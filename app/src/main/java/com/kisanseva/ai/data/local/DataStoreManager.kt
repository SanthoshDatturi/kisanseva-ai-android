package com.kisanseva.ai.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Base64

@Serializable
data class UserPreferences(
    val token: String? = null,
    val userId: String? = null
)

object UserPreferencesSerializer: Serializer<UserPreferences> {
    override val defaultValue: UserPreferences
        get() = UserPreferences()

    override suspend fun readFrom(input: InputStream): UserPreferences {
        val encryptedBytes = withContext(Dispatchers.IO) {
            input.use { it.readBytes() }
        }
        val encryptedBytesDecoded = Base64.getDecoder().decode(encryptedBytes)
        val decryptedBytes = Crypto.decrypt(encryptedBytesDecoded)
        val decodedJsonString = decryptedBytes.decodeToString()
        return Json.decodeFromString(decodedJsonString)
    }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        val json = Json.encodeToString(t)
        val bytes = json.toByteArray()
        val encryptedBytes = Crypto.encrypt(bytes)
        val encryptedBytesBase64 = Base64.getEncoder().encode(encryptedBytes)
        withContext(Dispatchers.IO) {
            output.use {
                it.write(encryptedBytesBase64)
            }
        }
    }
}

private val Context.userPreferencesStore: DataStore<UserPreferences> by dataStore(
    fileName = "user_prefs.json",
    serializer = UserPreferencesSerializer
)

class DataStoreManager(private val context: Context) {

    val token: Flow<String?> = context.userPreferencesStore.data
        .catch { exception ->
            if (exception is IOException) emit(UserPreferences()) else throw exception
        }
        .map { it.token }

    val userId: Flow<String?> = context.userPreferencesStore.data
        .catch { exception ->
            if (exception is IOException) emit(UserPreferences()) else throw exception
        }
        .map { it.userId }

    suspend fun saveToken(token: String) {
        context.userPreferencesStore.updateData { prefs ->
            prefs.copy(token = token)
        }
    }

    suspend fun saveUserId(userId: String) {
        context.userPreferencesStore.updateData { prefs ->
            prefs.copy(userId = userId)
        }
    }

    suspend fun clearToken() {
        context.userPreferencesStore.updateData { prefs ->
            prefs.copy(token = null, userId = null)
        }
    }
}
