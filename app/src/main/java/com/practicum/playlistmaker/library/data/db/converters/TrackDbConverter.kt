package com.practicum.playlistmaker.library.data.db.converters

import com.practicum.playlistmaker.library.data.db.entity.TrackEntity
import com.practicum.playlistmaker.search.domain.models.Track
import java.time.Instant

class TrackDbConverter {
    fun toTrackEntity(track: Track): TrackEntity {
        return TrackEntity(
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

    fun toTrack(entity: TrackEntity): Track {
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