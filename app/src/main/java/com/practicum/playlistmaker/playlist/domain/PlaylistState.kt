package com.practicum.playlistmaker.playlist.domain

import com.practicum.playlistmaker.library.domain.model.Playlist

sealed interface PlaylistState {
    data object Loading : PlaylistState

    data class Content(val playlist: Playlist) : PlaylistState

    data object Empty : PlaylistState
}