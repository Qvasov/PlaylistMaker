package com.practicum.playlistmaker.library.domain.impl

import com.practicum.playlistmaker.library.domain.api.PlaylistsInteractor
import com.practicum.playlistmaker.library.domain.api.PlaylistsRepository
import com.practicum.playlistmaker.library.domain.model.Playlist
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

class PlaylistInteractorImpl(
    private val playlistsRepository: PlaylistsRepository
) : PlaylistsInteractor {

    override suspend fun addPlaylist(playlist: Playlist) {
        playlistsRepository.addPlaylist(playlist)
    }

    override suspend fun deletePlaylist(playlist: Playlist) {
        playlistsRepository.deletePlaylist(playlist)
    }

    override suspend fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistsRepository.getAllPlaylists()
    }

    override suspend fun addToPlaylist(track: Track, playlist: Playlist) {
        playlistsRepository.addToPlaylist(track, playlist)
    }


}