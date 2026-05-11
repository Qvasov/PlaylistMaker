package com.practicum.playlistmaker.library.data

import com.practicum.playlistmaker.library.data.db.AppDatabase
import com.practicum.playlistmaker.library.data.db.converters.TrackDbConverter
import com.practicum.playlistmaker.library.domain.api.FavoritesRepository
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FavoritesRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val trackDbConverter: TrackDbConverter
) : FavoritesRepository {

    override suspend fun addTrack(track: Track) {
        appDatabase.trackDao().insertTrack(trackDbConverter.toTrackEntity(track))
    }

    override suspend fun deleteTrack(track: Track) {
        appDatabase.trackDao().deleteTrack(trackDbConverter.toTrackEntity(track))
    }

    override suspend fun getAllTracks(): Flow<List<Track>> = flow {
        emit(appDatabase.trackDao().getTracks()
            .sortedByDescending { it.addTimestamp }
            .map { trackDbConverter.toTrack(it) })
    }
}