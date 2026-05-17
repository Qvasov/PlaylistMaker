package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.library.data.FavoritesRepositoryImpl
import com.practicum.playlistmaker.library.data.PlaylistsRepositoryImpl
import com.practicum.playlistmaker.library.domain.api.FavoritesRepository
import com.practicum.playlistmaker.library.domain.api.PlaylistsRepository
import com.practicum.playlistmaker.search.data.TracksRepositoryImpl
import com.practicum.playlistmaker.search.domain.api.TracksRepository
import com.practicum.playlistmaker.settings.data.SettingsRepositoryImpl
import com.practicum.playlistmaker.settings.domain.SettingsRepository
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

private const val PREFERENCES = "preferences"
private const val HISTORY_DATA_KEY = "history"


val repositoryModule = module {
    factory<TracksRepository> {
        TracksRepositoryImpl(get(), get {
            parametersOf(HISTORY_DATA_KEY)
        }, get(), get())
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(get {
            parametersOf(PREFERENCES)
        })
    }

    single<FavoritesRepository> {
        FavoritesRepositoryImpl(get(), get())
    }

    single<PlaylistsRepository> {
        PlaylistsRepositoryImpl(get(), get())
    }
}
