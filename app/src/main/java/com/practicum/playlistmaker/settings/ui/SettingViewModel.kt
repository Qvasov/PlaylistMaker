package com.practicum.playlistmaker.settings.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.practicum.playlistmaker.App
import com.practicum.playlistmaker.creator.Creator

class SettingViewModel(private val context: Context): ViewModel() {
    companion object {
        fun getFactory() : ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY] as App)
                SettingViewModel(app)
            }
        }
    }

    val settingsRepository = Creator.provideSettingsRepository(context)
    var nightMode = false

    fun switchTheme(nightModeEnabled: Boolean) {
        nightMode = nightModeEnabled
        settingsRepository.saveNightModeStatus(nightModeEnabled)
        (context as App).switchTheme(nightModeEnabled)
    }
}