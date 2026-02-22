package com.kisanseva.ai.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.kisanseva.ai.BuildConfig
import com.kisanseva.ai.data.local.AppDatabase
import com.kisanseva.ai.data.local.DataStoreManager
import com.kisanseva.ai.data.remote.CultivatingCalendarApi
import com.kisanseva.ai.data.remote.websocket.WebSocketController
import com.kisanseva.ai.data.local.dao.ChatSessionDao
import com.kisanseva.ai.data.local.dao.CropRecommendationDao
import com.kisanseva.ai.data.local.dao.CultivatingCalendarDao
import com.kisanseva.ai.data.local.dao.CultivatingCropDao
import com.kisanseva.ai.data.local.dao.CurrentWeatherCacheDao
import com.kisanseva.ai.data.local.dao.FarmProfileDao
import com.kisanseva.ai.data.local.dao.ForecastCacheDao
import com.kisanseva.ai.data.local.dao.InterCroppingDetailsDao
import com.kisanseva.ai.data.local.dao.InvestmentBreakdownDao
import com.kisanseva.ai.data.local.dao.MessageDao
import com.kisanseva.ai.data.local.dao.PesticideRecommendationDao
import com.kisanseva.ai.data.local.dao.QueuedMessageDao
import com.kisanseva.ai.data.local.dao.ReverseGeocodingCacheDao
import com.kisanseva.ai.data.local.dao.SoilHealthRecommendationDao
import com.kisanseva.ai.data.remote.AuthApi
import com.kisanseva.ai.data.remote.AuthInterceptor
import com.kisanseva.ai.data.remote.ChatApi
import com.kisanseva.ai.data.remote.CropRecommendationApi
import com.kisanseva.ai.data.remote.CultivatingCropApi
import com.kisanseva.ai.data.remote.FarmApi
import com.kisanseva.ai.data.remote.FilesApi
import com.kisanseva.ai.data.remote.InvestmentBreakdownApi
import com.kisanseva.ai.data.remote.PesticideRecommendationApi
import com.kisanseva.ai.data.remote.SoilHealthRecommendationApi
import com.kisanseva.ai.data.remote.UserApi
import com.kisanseva.ai.data.remote.WeatherApi
import com.kisanseva.ai.data.repository.AuthRepositoryImpl
import com.kisanseva.ai.data.repository.ChatRepositoryImpl
import com.kisanseva.ai.data.repository.CropRecommendationRepositoryImpl
import com.kisanseva.ai.data.repository.CultivatingCalendarRepositoryImpl
import com.kisanseva.ai.data.repository.CultivatingCropRepositoryImpl
import com.kisanseva.ai.data.repository.FarmRepositoryImpl
import com.kisanseva.ai.data.repository.FilesRepositoryImpl
import com.kisanseva.ai.data.repository.InvestmentBreakdownRepositoryImpl
import com.kisanseva.ai.data.repository.PesticideRecommendationRepositoryImpl
import com.kisanseva.ai.data.repository.SoilHealthRecommendationRepositoryImpl
import com.kisanseva.ai.data.repository.UserRepositoryImpl
import com.kisanseva.ai.data.repository.WeatherRepositoryImpl
import com.kisanseva.ai.domain.repository.AuthRepository
import com.kisanseva.ai.domain.repository.ChatRepository
import com.kisanseva.ai.domain.repository.CropRecommendationRepository
import com.kisanseva.ai.domain.repository.CultivatingCalendarRepository
import com.kisanseva.ai.domain.repository.CultivatingCropRepository
import com.kisanseva.ai.domain.repository.FarmRepository
import com.kisanseva.ai.domain.repository.FilesRepository
import com.kisanseva.ai.domain.repository.InvestmentBreakdownRepository
import com.kisanseva.ai.domain.repository.PesticideRecommendationRepository
import com.kisanseva.ai.domain.repository.SoilHealthRecommendationRepository
import com.kisanseva.ai.domain.repository.UserRepository
import com.kisanseva.ai.domain.repository.WeatherRepository
import com.kisanseva.ai.system.audio.player.AndroidAudioPlayer
import com.kisanseva.ai.system.audio.player.AudioPlayer
import com.kisanseva.ai.system.storage.AndroidMediaStorageManager
import com.kisanseva.ai.system.storage.MediaStorageManager
import com.kisanseva.ai.util.ConnectivityObserver
import com.kisanseva.ai.util.NetworkConnectivityObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val HTTP_BASE_URL = "https://${BuildConfig.BASE_URL}"

    private val json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideAudioPlayer(
        @ApplicationContext context: Context
    ): AudioPlayer {
        return AndroidAudioPlayer(context)
    }

    @Provides
    @Singleton
    fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver {
        return NetworkConnectivityObserver(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "kisan_mithra_db"
            ).fallbackToDestructiveMigration(true).build()
    }

    @Provides
    @Singleton
    fun provideMessageDao(database: AppDatabase): MessageDao = database.messageDao()

    @Provides
    @Singleton
    fun provideChatSessionDao(database: AppDatabase): ChatSessionDao = database.chatSessionDao()

    @Provides
    @Singleton
    fun provideQueuedMessageDao(database: AppDatabase): QueuedMessageDao = database.queuedMessageDao()

    @Provides
    @Singleton
    fun provideInterCroppingDetailsDao(database: AppDatabase): InterCroppingDetailsDao = database.interCroppingDetailsDao()

    @Provides
    @Singleton
    fun provideCultivatingCropDao(database: AppDatabase): CultivatingCropDao = database.cultivatingCropDao()

    @Provides
    @Singleton
    fun provideCultivatingCalendarDao(database: AppDatabase): CultivatingCalendarDao = database.cultivatingCalendarDao()

    @Provides
    @Singleton
    fun provideSoilHealthRecommendationDao(database: AppDatabase): SoilHealthRecommendationDao = database.soilHealthRecommendationDao()

    @Provides
    @Singleton
    fun provideInvestmentBreakdownDao(database: AppDatabase): InvestmentBreakdownDao = database.investmentBreakdownDao()

    @Provides
    @Singleton
    fun provideCropRecommendationDao(database: AppDatabase): CropRecommendationDao = database.cropRecommendationDao()

    @Provides
    @Singleton
    fun provideFarmProfileDao(database: AppDatabase): FarmProfileDao = database.farmProfileDao()

    @Provides
    @Singleton
    fun provideCurrentWeatherCacheDao(database: AppDatabase): CurrentWeatherCacheDao = database.currentWeatherCacheDao()

    @Provides
    @Singleton
    fun provideForecastCacheDao(database: AppDatabase): ForecastCacheDao = database.forecastCacheDao()

    @Provides
    @Singleton
    fun provideReverseGeocodingCacheDao(database: AppDatabase): ReverseGeocodingCacheDao = database.reverseGeocodingCacheDao()

    @Provides
    @Singleton
    fun providePesticideRecommendationDao(database: AppDatabase): PesticideRecommendationDao = database.pesticideRecommendationDao()

    @Provides
    @Singleton
    fun provideAuthApi(@UnauthenticatedClient client: OkHttpClient): AuthApi =
        AuthApi(client, HTTP_BASE_URL, json)

    @Provides
    @Singleton
    fun provideUserApi(@AuthenticatedClient client: OkHttpClient): UserApi =
        UserApi(client, HTTP_BASE_URL, json)

    @Provides
    @Singleton
    fun provideChatApi(@AuthenticatedClient client: OkHttpClient): ChatApi =
        ChatApi(client, HTTP_BASE_URL, json)

    @Provides
    @Singleton
    fun provideFarmApi(@AuthenticatedClient client: OkHttpClient): FarmApi =
        FarmApi(client, HTTP_BASE_URL, json)

    @Provides
    @Singleton
    fun provideFilesApi(@AuthenticatedClient client: OkHttpClient): FilesApi =
        FilesApi(client, HTTP_BASE_URL, json)

    @Provides
    @Singleton
    fun provideCropRecommendationApi(@AuthenticatedClient client: OkHttpClient): CropRecommendationApi =
        CropRecommendationApi(client, HTTP_BASE_URL, json)

    @Provides
    @Singleton
    fun provideCultivatingCropApi(@AuthenticatedClient client: OkHttpClient): CultivatingCropApi =
        CultivatingCropApi(client, HTTP_BASE_URL, json)

    @Provides
    @Singleton
    fun provideCultivatingCalendarApi(@AuthenticatedClient client: OkHttpClient): CultivatingCalendarApi = CultivatingCalendarApi(client, HTTP_BASE_URL, json)

    @Provides
    @Singleton
    fun provideSoilHealthRecommendationApi(@AuthenticatedClient client: OkHttpClient): SoilHealthRecommendationApi =
        SoilHealthRecommendationApi(client, HTTP_BASE_URL, json)

    @Provides
    @Singleton
    fun provideInvestmentBreakdownApi(@AuthenticatedClient client: OkHttpClient): InvestmentBreakdownApi =
        InvestmentBreakdownApi(client, HTTP_BASE_URL, json)

    @Provides
    @Singleton
    fun provideWeatherApi(@AuthenticatedClient client: OkHttpClient): WeatherApi =
        WeatherApi(client, HTTP_BASE_URL, json)

    @Provides
    @Singleton
    fun providePesticideRecommendationApi(@AuthenticatedClient client: OkHttpClient): PesticideRecommendationApi =
        PesticideRecommendationApi(client, HTTP_BASE_URL, json)

    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager = DataStoreManager(context)

    @Provides
    @Singleton
    fun provideMediaStorageManager(@ApplicationContext context: Context): MediaStorageManager = AndroidMediaStorageManager(context)

    @Provides
    @Singleton
    @AuthenticatedClient
    fun provideAuthenticatedOkHttpClient(dataStoreManager: DataStoreManager): OkHttpClient {
        val logging = HttpLoggingInterceptor { message -> Log.d("OkHttp", message) }
        logging.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(dataStoreManager))
            .build()
    }

    @Provides
    @Singleton
    @UnauthenticatedClient
    fun provideUnauthenticatedOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor { message -> Log.d("UOkHttp", message) }
        logging.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApi, ds: DataStoreManager): AuthRepository =
        AuthRepositoryImpl(api, ds)

    @Provides
    @Singleton
    fun provideUserRepository(userApi: UserApi): UserRepository = UserRepositoryImpl(userApi)

    @Provides
    @Singleton
    fun provideChatRepository(
        chatApi: ChatApi,
        webSocketController: WebSocketController,
        messageDao: MessageDao,
        queuedMessageDao: QueuedMessageDao,
        mediaStorageManager: MediaStorageManager,
        chatSessionDao: ChatSessionDao
    ): ChatRepository = ChatRepositoryImpl(
        chatApi,
        webSocketController,
        messageDao,
        queuedMessageDao,
        mediaStorageManager,
        chatSessionDao
    )

    @Provides
    @Singleton
    fun provideFarmRepository(farmApi: FarmApi, farmProfileDao: FarmProfileDao): FarmRepository =
        FarmRepositoryImpl(farmApi, farmProfileDao)

    @Provides
    @Singleton
    fun provideFilesRepository(filesApi: FilesApi): FilesRepository = FilesRepositoryImpl(filesApi)

    @Provides
    @Singleton
    fun provideCropRecommendationRepository(
        cropRecommendationApi: CropRecommendationApi,
        webSocketController: WebSocketController,
        cropRecommendationDao: CropRecommendationDao,
        cultivatingCropRepository: CultivatingCropRepository
    ): CropRecommendationRepository = CropRecommendationRepositoryImpl(
        cropRecommendationApi,
        webSocketController,
        cropRecommendationDao,
        cultivatingCropRepository
    )

    @Provides
    @Singleton
    fun provideCultivatingCropRepository(
        cultivatingCropApi: CultivatingCropApi,
        cultivatingCropDao: CultivatingCropDao,
        interCroppingDetailsDao: InterCroppingDetailsDao
    ): CultivatingCropRepository = CultivatingCropRepositoryImpl(
        cultivatingCropApi,
        cultivatingCropDao,
        interCroppingDetailsDao
    )

    @Provides
    @Singleton
    fun provideCultivatingCalendarRepository(
        api: CultivatingCalendarApi,
        dao: CultivatingCalendarDao
    ): CultivatingCalendarRepository = CultivatingCalendarRepositoryImpl(api, dao)

    @Provides
    @Singleton
    fun provideSoilHealthRecommendationRepository(
        api: SoilHealthRecommendationApi,
        dao: SoilHealthRecommendationDao
    ): SoilHealthRecommendationRepository = SoilHealthRecommendationRepositoryImpl(api, dao)

    @Provides
    @Singleton
    fun provideInvestmentBreakdownRepository(
        api: InvestmentBreakdownApi,
        dao: InvestmentBreakdownDao
    ): InvestmentBreakdownRepository = InvestmentBreakdownRepositoryImpl(api, dao)

    @Provides
    @Singleton
    fun provideWeatherRepository(
        api: WeatherApi,
        currentWeatherCacheDao: CurrentWeatherCacheDao,
        forecastCacheDao: ForecastCacheDao,
        reverseGeocodingCacheDao: ReverseGeocodingCacheDao
    ): WeatherRepository = WeatherRepositoryImpl(
        api,
        currentWeatherCacheDao,
        forecastCacheDao,
        reverseGeocodingCacheDao
    )

    @Provides
    @Singleton
    fun providePesticideRecommendationRepository(
        api: PesticideRecommendationApi,
        dao: PesticideRecommendationDao,
        webSocketController: WebSocketController
    ): PesticideRecommendationRepository =
        PesticideRecommendationRepositoryImpl(api, dao, webSocketController)
}
