package com.practicum.playlistmaker.library.domain.api

import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    suspend fun addTrack(track: Track)

    suspend fun deleteTrack(track: Track)

    suspend fun getAllTracks(): Flow<List<Track>>
}