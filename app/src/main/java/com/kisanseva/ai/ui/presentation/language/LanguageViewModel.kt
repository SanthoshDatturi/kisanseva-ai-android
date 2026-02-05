package com.kisanseva.ai.ui.presentation.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel that manages app language state (MVVM + SRP).
 */
@HiltViewModel
class LanguageViewModel @Inject constructor() : ViewModel() {

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language

    init {
        viewModelScope.launch {
            val locales = AppCompatDelegate.getApplicationLocales()
            if (!locales.isEmpty) {
                _language.value = locales[0]?.language ?: "en"
            } else {
                _language.value = "en"
            }
        }
    }

    fun changeLanguage(langCode: String) {
        viewModelScope.launch {
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(langCode)
            AppCompatDelegate.setApplicationLocales(appLocale)
            _language.value = langCode
        }
    }
}