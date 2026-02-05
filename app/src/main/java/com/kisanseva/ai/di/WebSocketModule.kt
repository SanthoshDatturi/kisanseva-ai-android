package com.kisanmithra.app.di

import com.kisanseva.ai.data.local.DataStoreManager
import com.kisanseva.ai.data.remote.websocket.WebSocketController
import com.kisanseva.ai.di.AuthenticatedClient
import com.kisanseva.ai.util.ConnectivityObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebSocketModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideWebSocketController(
        @AuthenticatedClient okHttpClient: OkHttpClient,
        dataStoreManager: DataStoreManager,
        connectivityObserver: ConnectivityObserver,
        json: Json
    ): WebSocketController {
        return WebSocketController(okHttpClient, dataStoreManager, connectivityObserver, json)
    }
}
