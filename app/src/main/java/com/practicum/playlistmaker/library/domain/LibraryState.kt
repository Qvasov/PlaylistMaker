package com.practicum.playlistmaker.library.domain

import com.practicum.playlistmaker.search.domain.models.Track

sealed interface LibraryState {
    data object Loading : LibraryState

    data class Content(val trackList: List<Track>) : LibraryState

    data object Empty : LibraryState
}