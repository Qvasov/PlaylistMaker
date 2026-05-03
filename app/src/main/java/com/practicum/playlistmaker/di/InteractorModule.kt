package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.search.domain.api.TracksInteractor
import com.practicum.playlistmaker.search.domain.impl.TracksInteractorImpl
import org.koin.dsl.module


val interactorModule = module {
    factory<TracksInteractor> {
        TracksInteractorImpl(get())
    }
}
