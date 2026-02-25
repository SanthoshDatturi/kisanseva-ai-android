package com.kisanseva.ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
import com.kisanseva.ai.data.local.dao.UserDao
import com.kisanseva.ai.data.local.entity.ChatSessionEntity
import com.kisanseva.ai.data.local.entity.CropRecommendationEntity
import com.kisanseva.ai.data.local.entity.CultivatingCalendarEntity
import com.kisanseva.ai.data.local.entity.CultivatingCropEntity
import com.kisanseva.ai.data.local.entity.CurrentWeatherCacheEntity
import com.kisanseva.ai.data.local.entity.FarmProfileEntity
import com.kisanseva.ai.data.local.entity.ForecastCacheEntity
import com.kisanseva.ai.data.local.entity.InterCropRecommendationEntity
import com.kisanseva.ai.data.local.entity.InterCroppingDetailsEntity
import com.kisanseva.ai.data.local.entity.InvestmentBreakdownEntity
import com.kisanseva.ai.data.local.entity.MessageEntity
import com.kisanseva.ai.data.local.entity.MonoCropEntity
import com.kisanseva.ai.data.local.entity.PesticideRecommendationEntity
import com.kisanseva.ai.data.local.entity.QueuedMessageEntity
import com.kisanseva.ai.data.local.entity.ReverseGeocodingCacheEntity
import com.kisanseva.ai.data.local.entity.SoilHealthRecommendationEntity
import com.kisanseva.ai.data.local.entity.UserEntity

@Database(
    entities = [
        MessageEntity::class,
        CropRecommendationEntity::class,
        MonoCropEntity::class,
        InterCropRecommendationEntity::class,
        FarmProfileEntity::class,
        CultivatingCropEntity::class,
        InterCroppingDetailsEntity::class,
        QueuedMessageEntity::class,
        ChatSessionEntity::class,
        CurrentWeatherCacheEntity::class,
        ForecastCacheEntity::class,
        ReverseGeocodingCacheEntity::class,
        CultivatingCalendarEntity::class,
        SoilHealthRecommendationEntity::class,
        InvestmentBreakdownEntity::class,
        PesticideRecommendationEntity::class,
        UserEntity::class
    ], version = 14,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun cropRecommendationDao(): CropRecommendationDao
    abstract fun farmProfileDao(): FarmProfileDao
    abstract fun cultivatingCropDao(): CultivatingCropDao
    abstract fun interCroppingDetailsDao(): InterCroppingDetailsDao
    abstract fun queuedMessageDao(): QueuedMessageDao
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun currentWeatherCacheDao(): CurrentWeatherCacheDao
    abstract fun forecastCacheDao(): ForecastCacheDao
    abstract fun reverseGeocodingCacheDao(): ReverseGeocodingCacheDao
    abstract fun cultivatingCalendarDao(): CultivatingCalendarDao
    abstract fun soilHealthRecommendationDao(): SoilHealthRecommendationDao
    abstract fun investmentBreakdownDao(): InvestmentBreakdownDao
    abstract fun pesticideRecommendationDao(): PesticideRecommendationDao
    abstract fun userDao(): UserDao
}
