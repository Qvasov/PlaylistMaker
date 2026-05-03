package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.search.ui.TrackAdapter
import org.koin.dsl.module

val uiModule = module {
    single<TrackAdapter> { params ->
        TrackAdapter(params.get())
    }
}