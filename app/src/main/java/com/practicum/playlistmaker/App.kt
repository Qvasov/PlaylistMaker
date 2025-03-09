package com.practicum.playlistmaker

import android.app.Application
import android.app.UiModeManager
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {
    companion object {
        val NIGHT_THEME = "night_theme"
    }
    var darkTheme = false

    override fun onCreate() {
        super.onCreate()

        val sharedPrefs = getSharedPreferences(NIGHT_THEME, MODE_PRIVATE)
        val nightTheme = sharedPrefs.getString(NIGHT_THEME, null)

        if (nightTheme == null) {
            val uiModeManager = this.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
            if (uiModeManager.nightMode == UiModeManager.MODE_NIGHT_YES) {
                darkTheme = true
            } else {
                darkTheme = false
            }
        } else {
            darkTheme = nightTheme.toBoolean()
        }

        switchTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled : Boolean) {
        darkTheme = darkThemeEnabled

        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

    }
}