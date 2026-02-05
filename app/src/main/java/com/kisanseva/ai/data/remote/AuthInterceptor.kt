package com.kisanseva.ai.data.remote

import com.kisanseva.ai.data.local.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

// AuthInterceptor.kt
class AuthInterceptor(private val dataStoreManager: DataStoreManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = runBlocking {
            dataStoreManager.token.first()
        }
        val req = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else original
        return chain.proceed(req)
    }
}