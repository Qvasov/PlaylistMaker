package com.practicum.playlistmaker

import android.app.Application
import android.app.UiModeManager
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.creator.Creator
import com.practicum.playlistmaker.settings.domain.SettingsRepository

class App : Application() {
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        settingsRepository = Creator.provideSettingsRepository(this)

        val nightMode = settingsRepository.getNightModeStatus()
        if (nightMode == null) {
            val uiModeManager = this.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
            if (uiModeManager.nightMode == UiModeManager.MODE_NIGHT_YES) {
                switchTheme(true)
            } else {
                switchTheme(false)
            }
        } else {
            switchTheme(nightMode.toBoolean())
        }
    }

    fun switchTheme(darkThemeEnabled : Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    companion object {
        const val PREFERENCES = "preferences"
        const val NIGHT_THEME = "night_theme"
    }
}