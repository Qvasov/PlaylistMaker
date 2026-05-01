package com.practicum.playlistmaker.settings.domain

interface SettingsRepository {
    fun saveNightModeStatus(enabled: Boolean)
}