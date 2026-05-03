package com.practicum.playlistmaker.settings.ui

import android.app.UiModeManager
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.practicum.playlistmaker.App
import com.practicum.playlistmaker.creator.Creator

class SettingViewModel(private val context: Context): ViewModel() {
    private val settingsRepository = Creator.provideSettingsRepository(context)

    private val nightMode = MutableLiveData<Boolean>()

    init {
        val status = settingsRepository.getNightModeStatus()
        if (status == null) {
            val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
            if (uiModeManager.nightMode == UiModeManager.MODE_NIGHT_YES) {
                nightMode.postValue(true)
            } else {
                nightMode.postValue(false)
            }
        } else {
            nightMode.postValue(status.toBoolean())
        }
    }

    fun observeNightMode(): LiveData<Boolean> = nightMode

    fun switchTheme(nightModeEnabled: Boolean) {
        nightMode.postValue(nightModeEnabled)
        settingsRepository.saveNightModeStatus(nightModeEnabled)
    }

    companion object {
        fun getFactory() : ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY] as App)
                SettingViewModel(app)
            }
        }
    }
}