package com.practicum.playlistmaker.search.domain.impl

import com.practicum.playlistmaker.search.domain.api.TracksInteractor
import com.practicum.playlistmaker.search.domain.api.TracksRepository
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

class TracksInteractorImpl(private val repository: TracksRepository) : TracksInteractor {

    override fun search(expression: String): Flow<List<Track>?> {
        return repository.search(expression)
    }

    override suspend fun saveToHistory(track: Track) {
        repository.saveToHistory(track)
    }

    override fun getHistory(): Flow<List<Track>?> {
        return repository.getHistory()
    }

    override suspend fun clearHistory() {
        repository.clearHistory()
    }
}