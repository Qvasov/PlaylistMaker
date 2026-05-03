package com.practicum.playlistmaker

import android.app.Application
import android.app.UiModeManager
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.di.dataModule
import com.practicum.playlistmaker.di.interactorModule
import com.practicum.playlistmaker.di.repositoryModule
import com.practicum.playlistmaker.di.uiModule
import com.practicum.playlistmaker.di.utilsModule
import com.practicum.playlistmaker.di.viewModelModule
import com.practicum.playlistmaker.settings.domain.SettingsRepository
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin{
            androidContext(this@App)
            modules(dataModule, repositoryModule, interactorModule, viewModelModule, uiModule, utilsModule)
        }

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
}