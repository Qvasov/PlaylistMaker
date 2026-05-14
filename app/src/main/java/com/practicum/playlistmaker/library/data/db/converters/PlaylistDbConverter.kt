package com.practicum.playlistmaker.library.data.db.converters

import android.net.Uri
import com.google.gson.Gson
import com.practicum.playlistmaker.library.data.db.entity.PlaylistEntity
import com.practicum.playlistmaker.library.data.db.entity.PlaylistTrackEntity
import com.practicum.playlistmaker.library.domain.model.Playlist
import com.practicum.playlistmaker.search.domain.models.Track
import java.lang.reflect.Type
import java.time.Instant

class PlaylistDbConverter(
    private val gson: Gson,
    private val type: Type,
) {
    fun toPlaylistEntity(playlist: Playlist): PlaylistEntity {
        return PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            coverUri = playlist.coverUri.toString(),
            trackIds = gson.toJson(playlist.trackIds, type),
            tracksCount = playlist.tracksCount
        )
    }

    fun toPlaylist(entity: PlaylistEntity): Playlist {
        return Playlist(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            coverUri = Uri.parse(entity.coverUri),
            trackIds = gson.fromJson(entity.trackIds, type),
            tracksCount = entity.tracksCount
        )
    }

    fun toPlaylistTrackEntity(track: Track): PlaylistTrackEntity {
        return PlaylistTrackEntity(
            trackId = track.trackId,
            artworkUrl100 = track.artworkUrl100,
            trackName = track.trackName,
            artistName = track.artistName,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            country = track.country,
            trackTime = track.trackTime,
            primaryGenreName = track.primaryGenreName,
            previewUrl = track.previewUrl,
            addTimestamp = Instant.now().toEpochMilli()
        )
    }

    fun toPlaylistTrack(entity: PlaylistTrackEntity): Track {
        return Track(
            trackId = entity.trackId,
            artworkUrl100 = entity.artworkUrl100,
            trackName = entity.trackName,
            artistName = entity.artistName,
            collectionName = entity.collectionName,
            releaseDate = entity.releaseDate,
            country = entity.country,
            trackTime = entity.trackTime,
            primaryGenreName = entity.primaryGenreName,
            previewUrl = entity.previewUrl,
            isFavorite = true
        )
    }
}