package com.practicum.playlistmaker.di

import android.os.Handler
import android.os.Looper
import org.koin.dsl.module


val utilsModule = module {
    single<Handler> {
        Handler(Looper.getMainLooper())
    }
}
