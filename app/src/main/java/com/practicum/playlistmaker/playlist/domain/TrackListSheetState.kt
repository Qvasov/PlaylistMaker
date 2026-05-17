package com.practicum.playlistmaker.playlist.domain

import com.practicum.playlistmaker.search.domain.models.Track
import java.text.SimpleDateFormat
import java.util.Locale

sealed interface TrackListSheetState {
    data object Loading : TrackListSheetState

    data class Content(val trackList: List<Track>) : TrackListSheetState {
        fun getTrackListTime(): String {
            return SimpleDateFormat("mm", Locale.getDefault()).format(trackList.sumOf { track -> track.trackTime })
        }
    }

    data object Empty : TrackListSheetState
}