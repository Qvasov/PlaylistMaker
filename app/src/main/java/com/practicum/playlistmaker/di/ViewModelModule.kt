package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.library.ui.CreatePlaylistViewModel
import com.practicum.playlistmaker.library.ui.EditPlaylistViewModel
import com.practicum.playlistmaker.library.ui.FavoritesViewModel
import com.practicum.playlistmaker.library.ui.PlaylistsViewModel
import com.practicum.playlistmaker.player.ui.PlayerViewModel
import com.practicum.playlistmaker.playlist.ui.PlaylistViewModel
import com.practicum.playlistmaker.search.ui.SearchViewModel
import com.practicum.playlistmaker.settings.ui.SettingViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val viewModelModule = module {
    viewModel {
        SearchViewModel(get())
    }

    viewModel {
        SettingViewModel(androidContext(), get())
    }

    viewModel {
        PlayerViewModel(get(), get())
    }

    viewModel {
        PlaylistsViewModel(get())
    }

    viewModel {
        FavoritesViewModel(get())
    }

    viewModel {
        CreatePlaylistViewModel(androidContext(), get())
    }

    viewModel {
        PlaylistViewModel(get())
    }

    viewModel {
        EditPlaylistViewModel(get(), get())
    }
}
