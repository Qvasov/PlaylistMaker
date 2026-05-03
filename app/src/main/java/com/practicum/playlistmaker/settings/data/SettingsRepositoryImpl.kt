package com.practicum.playlistmaker.settings.data

import android.content.SharedPreferences
import com.practicum.playlistmaker.settings.domain.SettingsRepository

class SettingsRepositoryImpl(
    private val sharedPreferences: SharedPreferences,
) : SettingsRepository {

    override fun saveNightModeStatus(enabled: Boolean) {
        sharedPreferences.edit()
            .putString(NIGHT_THEME, enabled.toString())
            .apply()
    }

    override fun getNightModeStatus(): String? {
        return sharedPreferences.getString(NIGHT_THEME, null)
    }

    private companion object {
        const val NIGHT_THEME = "night_theme"
    }
}