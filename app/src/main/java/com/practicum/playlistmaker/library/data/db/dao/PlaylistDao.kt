package com.practicum.playlistmaker.library.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.practicum.playlistmaker.library.data.db.entity.PlaylistEntity
import com.practicum.playlistmaker.library.data.db.entity.PlaylistTrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlistEntity: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlistEntity: PlaylistEntity)

    @Query("SELECT * FROM playlists WHERE id = :id")
    fun getPlaylistById(id: Long): Flow<PlaylistEntity>

    @Query("SELECT * FROM playlists")
    fun getPlaylists() : Flow<List<PlaylistEntity>>

    @Query("SELECT trackIds FROM playlists")
    suspend fun getTrackIdsFromPlayLists() : String

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylistTrack(playlistTrackEntity: PlaylistTrackEntity)

    @Delete
    suspend fun deletePlaylistTrack(playlistTrackEntity: PlaylistTrackEntity)

    @Transaction
    suspend fun addTrackToPlaylist(playlistTrackEntity: PlaylistTrackEntity, playlistEntity: PlaylistEntity) {
        insertPlaylistTrack(playlistTrackEntity)
        insertPlaylist(playlistEntity)
    }

    @Transaction
    suspend fun deleteTrackFromPlaylist(playlistTrackEntity: PlaylistTrackEntity, playlistEntity: PlaylistEntity) {

        insertPlaylist(playlistEntity)
    }

    @Query("SELECT * FROM playlist_track WHERE trackId IN (:trackIdList)")
    fun getTracksById(trackIdList: List<Long>): Flow<List<PlaylistTrackEntity>>
}