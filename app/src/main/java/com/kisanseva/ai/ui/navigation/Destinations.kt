package com.kisanseva.ai.ui.navigation

import com.kisanseva.ai.domain.model.ChatType
import kotlinx.serialization.Serializable


@Serializable
sealed interface AppDest {
    @Serializable
    object Splash : AppDest

    @Serializable
    data class LanguageSelection(val fromSettings: Boolean = false) : AppDest

    @Serializable
    object Auth : AppDest

    @Serializable
    object Main : AppDest
}

@Serializable
sealed interface MainDest {
    @Serializable object Home : MainDest
    @Serializable object FarmList : MainDest
    @Serializable object ChatList : MainDest
    @Serializable object Settings : MainDest
    @Serializable object AlertList : MainDest
}

@Serializable
sealed interface FarmDest {
    @Serializable data class FarmProfile(val farmId: String) : FarmDest
    @Serializable
    data class CultivatingCrop(val cropId: String) : FarmDest
    @Serializable
    data class CultivationCalendar(val cropId: String): FarmDest
    @Serializable
    data class SoilHealthRecommendations(val cropId: String): FarmDest
    @Serializable
    data class InvestmentBreakdown(val cropId: String): FarmDest
    @Serializable data class InterCroppingDetails(val intercroppingId: String) : FarmDest
    @Serializable data class CropRecommendation(val farmId: String) : FarmDest
    @Serializable data class RecommendedMonoCropDetails(
        val monoCropId: String,
        val farmId: String,
        val cropRecommendationResponseId: String
    ) : FarmDest
    @Serializable data class RecommendedInterCropDetails(
        val interCropId: String,
        val farmId: String,
        val cropRecommendationResponseId: String
    ) : FarmDest
}

@Serializable
sealed interface PesticideDest {
    @Serializable data class PesticideList(val cropId: String, val farmId: String) : PesticideDest
    @Serializable data class PesticideRecommendation(val recommendationId: String) : PesticideDest
}

@Serializable
sealed interface ChatDest {
    @Serializable data class Chat(
        val chatId: String? = null, val chatType: ChatType? = null, val dataId: String? = null
    ) : ChatDest
}

@Serializable
sealed interface SettingDest {
    @Serializable object Language : SettingDest
}


@Serializable
sealed interface AlertDest {
    @Serializable data class Alert(val alertId: String) : AlertDest
}
