package com.practicum.playlistmaker.library.domain

import com.practicum.playlistmaker.library.domain.model.Playlist

sealed interface PlaylistsState {
    data object Loading : PlaylistsState

    data class Content(val playlists: List<Playlist>) : PlaylistsState

    data object Empty : PlaylistsState
}