package com.practicum.playlistmaker.library.domain.model

sealed interface EditPlaylistState {
    data object Loading : EditPlaylistState

    data class Content(val playlist: Playlist) : EditPlaylistState

    data object Empty : EditPlaylistState
}