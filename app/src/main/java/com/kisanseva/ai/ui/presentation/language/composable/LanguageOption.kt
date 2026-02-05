package com.kisanseva.ai.ui.presentation.language.composable

data class LanguageOption(
    val code: String,
    val displayName: String,
    val nativeName: String,
)

val languageOptions = listOf(
    LanguageOption("en", "English", "English"),
    LanguageOption("kn", "Kannada", "ಕನ್ನಡ"),
    LanguageOption("te", "Telugu", "తెలుగు"),
    LanguageOption("hi-IN", "Hindi", "हिंदी"),
    LanguageOption("mr", "Marathi", "मराठी"),
    LanguageOption("gu", "Gujarati", "ગુજરાતી"),
    LanguageOption("ta", "Tamil", "தமிழ்"),
    LanguageOption("pa", "Punjabi", "ਪੰਜਾਬੀ"),
    LanguageOption("ml", "Malayalam", "മലയാളം"),
    LanguageOption("bn", "Bangla", "বাংলা")
)
