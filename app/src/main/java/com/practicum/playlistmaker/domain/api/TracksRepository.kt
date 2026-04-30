package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.models.Track

interface TracksRepository {
    fun search(expression: String): List<Track>?

    fun saveToHistory(track: Track)

    fun getHistory() : List<Track>

    fun clearHistory()

}