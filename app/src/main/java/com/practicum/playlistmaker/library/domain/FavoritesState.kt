package com.practicum.playlistmaker.library.domain

import com.practicum.playlistmaker.search.domain.models.Track

sealed interface FavoritesState {
    data object Loading : FavoritesState

    data class Content(val trackList: List<Track>) : FavoritesState

    data object Empty : FavoritesState
}