package com.daviddf.geeklab.ui.screens.settings

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit

enum class ThemeMode(val mode: Int) {
    SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
    DARK(AppCompatDelegate.MODE_NIGHT_YES)
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        ThemeMode.entries.find { it.mode == prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) } ?: ThemeMode.SYSTEM
    )
    val themeMode = _themeMode.asStateFlow()

    private val _currentLanguage = MutableStateFlow(getCurrentLanguageCode())
    val currentLanguage = _currentLanguage.asStateFlow()

    private fun getCurrentLanguageCode(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (!locales.isEmpty) {
            locales.get(0)?.language ?: "en"
        } else {
            val systemLocale = getApplication<Application>().resources.configuration.locales[0].language
            // If the system is Spanish, we use Spanish. Otherwise, we default to English.
            if (systemLocale == "es") "es" else "en"
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit { putInt("theme_mode", mode.mode) }
        AppCompatDelegate.setDefaultNightMode(mode.mode)
    }

    fun setLanguage(languageCode: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
        // We don't manually update _currentLanguage here because the activity will recreate
        // and re-sync from getCurrentLanguageCode() via refreshLanguageState()
    }

    // Call this from activity to ensure flow is in sync with system
    fun refreshLanguageState() {
        _currentLanguage.value = getCurrentLanguageCode()
    }
}
