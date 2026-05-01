package com.practicum.playlistmaker.settings.data

import android.content.Context
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import com.practicum.playlistmaker.App
import com.practicum.playlistmaker.settings.domain.SettingsRepository

class SettingsRepositoryImpl(
    private val context: Context,
) : SettingsRepository {

    val sharedPreferences = context.getSharedPreferences(App.PREFERENCES, MODE_PRIVATE)

    override fun saveNightModeStatus(enabled: Boolean) {
        sharedPreferences.edit()
            .putString(App.NIGHT_THEME, enabled.toString())
            .apply()
    }
}