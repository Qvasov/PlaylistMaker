package com.practicum.playlistmaker.search.ui

import com.practicum.playlistmaker.search.domain.models.Track

sealed interface SearchState {
    data object Loading : SearchState

    data class Content(val trackList: List<Track>) : SearchState

    data class History(val trackList: List<Track>) : SearchState

    data object Error : SearchState

    data object Empty : SearchState

}