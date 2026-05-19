package com.practicum.playlistmaker.library.domain.api

import com.practicum.playlistmaker.library.domain.model.Playlist
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsInteractor {

    suspend fun addPlaylist(playlist: Playlist)

    suspend fun deletePlaylist(playlist: Playlist)

    suspend fun getPlaylistById(id: Long): Flow<Playlist>

    suspend fun getAllPlaylists(): Flow<List<Playlist>>

    suspend fun addToPlaylist(track: Track, playlist: Playlist)

    suspend fun deleteFromPlaylist(track: Track, playlist: Playlist)

    suspend fun getTrackListById(playlistId: Long): Flow<List<Track>>
}