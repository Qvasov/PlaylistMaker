package com.practicum.playlistmaker.search.domain.api

import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface TracksInteractor {
    fun search(expression: String) : Flow<List<Track>?>

    suspend fun saveToHistory(track: Track)

    fun getHistory(): Flow<List<Track>?>

    suspend fun clearHistory()
}