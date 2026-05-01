package com.practicum.playlistmaker.search.domain.impl

import com.practicum.playlistmaker.search.domain.api.TracksInteractor
import com.practicum.playlistmaker.search.domain.api.TracksRepository
import com.practicum.playlistmaker.search.domain.models.Track
import java.util.concurrent.Executors

class TracksInteractorImpl(private val repository: TracksRepository) : TracksInteractor {
    private val executor = Executors.newCachedThreadPool()

    override fun search(expression: String, consumer: TracksInteractor.TracksConsumer) {
        executor.execute {
            consumer.consume(repository.search(expression))
        }
    }

    override fun saveToHistory(track: Track) {
        executor.execute {
            repository.saveToHistory(track)
        }
    }

    override fun getHistory(consumer: TracksInteractor.TracksConsumer) {
        executor.execute {
            consumer.consume(repository.getHistory())
        }
    }

    override fun clearHistory() {
        executor.execute {
            repository.clearHistory()
        }
    }
}