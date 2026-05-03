package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.di.RepositoryModule.HISTORY_DATA_KEY
import com.practicum.playlistmaker.di.RepositoryModule.PREFERENCES
import com.practicum.playlistmaker.search.data.TracksRepositoryImpl
import com.practicum.playlistmaker.search.domain.api.TracksRepository
import com.practicum.playlistmaker.settings.data.SettingsRepositoryImpl
import com.practicum.playlistmaker.settings.domain.SettingsRepository
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

private object RepositoryModule {
    const val PREFERENCES = "preferences"
    const val HISTORY_DATA_KEY = "history"
}


val repositoryModule = module {
    single<TracksRepository> {
        TracksRepositoryImpl(get(), get {
            parametersOf(HISTORY_DATA_KEY)
        })
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(get {
            parametersOf(PREFERENCES)
        })
    }
}
