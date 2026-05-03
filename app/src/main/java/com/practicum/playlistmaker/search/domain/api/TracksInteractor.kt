package com.practicum.playlistmaker.search.domain.api

import com.practicum.playlistmaker.search.domain.models.Track

interface TracksInteractor {
    fun search(expression: String, consumer: TracksConsumer)

    fun saveToHistory(track: Track)

    fun getHistory(consumer: TracksConsumer)

    fun clearHistory()

    interface TracksConsumer {
        fun consume(foundTracks: List<Track>?)
    }
}