package com.practicum.playlistmaker.settings.ui

import android.app.UiModeManager
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.settings.domain.SettingsRepository


class SettingViewModel(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

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
}