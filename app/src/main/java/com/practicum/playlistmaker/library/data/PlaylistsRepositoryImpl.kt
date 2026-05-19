package com.practicum.playlistmaker.library.data

import com.practicum.playlistmaker.library.data.db.AppDatabase
import com.practicum.playlistmaker.library.data.db.converters.PlaylistDbConverter
import com.practicum.playlistmaker.library.domain.api.PlaylistsRepository
import com.practicum.playlistmaker.library.domain.model.Playlist
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PlaylistsRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val playlistDbConverter: PlaylistDbConverter
) : PlaylistsRepository {

    override suspend fun addPlaylist(playlist: Playlist) {
        appDatabase.playlistDao().insertPlaylist(playlistDbConverter.toPlaylistEntity(playlist))
    }

    override suspend fun deletePlaylist(playlist: Playlist) {
        appDatabase.playlistDao().deletePlaylist(playlistDbConverter.toPlaylistEntity(playlist))
    }

    override suspend fun getPlaylistById(id: Long): Flow<Playlist> {
        return appDatabase.playlistDao().getPlaylistById(id)
            .map { playlistDbConverter.toPlaylist(it) }
    }

    override suspend fun getAllPlaylists(): Flow<List<Playlist>> {
        return appDatabase.playlistDao().getPlaylists()
            .map { playlistEntityList ->
                playlistEntityList
                    .map { playlistEntity -> playlistDbConverter.toPlaylist(playlistEntity) }
            }
    }

    override suspend fun addToPlaylist(track: Track, playlist: Playlist) {
        val playlistTrackEntity = playlistDbConverter.toPlaylistTrackEntity(track)
        val playlistEntity = playlistDbConverter.toPlaylistEntity(
            Playlist(
                playlist.id,
                playlist.name,
                playlist.description,
                playlist.coverUri,
                playlist.trackIds + track.trackId,
                playlist.tracksCount + 1
            )
        )
        appDatabase.playlistDao().addTrackToPlaylist(playlistTrackEntity, playlistEntity)
    }

    override suspend fun deleteFromPlaylist(track: Track, playlist: Playlist) {
        val playlistEntity = playlistDbConverter.toPlaylistEntity(
            Playlist(
                playlist.id,
                playlist.name,
                playlist.description,
                playlist.coverUri,
                playlist.trackIds - track.trackId,
                playlist.tracksCount - 1
            )
        )
        appDatabase.playlistDao().insertPlaylist(playlistEntity)

        getAllPlaylists().collect { playlists ->
            if (!playlists.any { it.trackIds.contains(track.trackId) })
                appDatabase.playlistDao()
                    .deletePlaylistTrack(playlistDbConverter.toPlaylistTrackEntity(track))
        }
    }

    override suspend fun getTrackListById(playlistId: Long): Flow<List<Track>> {
        val favoritesTracks = appDatabase.trackDao().getTrackIds()
        return getPlaylistById(playlistId)
            .map { playlist ->
                appDatabase.playlistDao().getTracksById(playlist.trackIds)
                    .map { trackEntityList ->
                        trackEntityList
                            .map { trackEntity ->
                                playlistDbConverter.toPlaylistTrack(
                                    trackEntity,
                                    favoritesTracks.contains(trackEntity.trackId)
                                )
                            }
                    }
                    .first()
            }

    }
}