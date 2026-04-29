package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.models.Track

interface TracksInteractor {
    fun search(expression: String, consumer: TracksConsumer)

    fun saveToHistory(track: Track)

    fun getHistory(consumer: TracksConsumer)

    fun clearHistory()

    interface TracksConsumer {
        fun consume(foundTracks: List<Track>?)
    }
}