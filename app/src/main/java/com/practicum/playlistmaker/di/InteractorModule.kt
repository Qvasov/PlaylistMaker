package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.library.domain.api.FavoritesInteractor
import com.practicum.playlistmaker.library.domain.api.PlaylistsInteractor
import com.practicum.playlistmaker.library.domain.impl.FavoritesInteractorImpl
import com.practicum.playlistmaker.library.domain.impl.PlaylistInteractorImpl
import com.practicum.playlistmaker.search.domain.api.TracksInteractor
import com.practicum.playlistmaker.search.domain.impl.TracksInteractorImpl
import org.koin.dsl.module


val interactorModule = module {
    factory<TracksInteractor> {
        TracksInteractorImpl(get())
    }

    factory<FavoritesInteractor> {
        FavoritesInteractorImpl(get())
    }

    factory<PlaylistsInteractor> {
        PlaylistInteractorImpl(get())
    }
}
