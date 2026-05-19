package com.practicum.playlistmaker.search.data.converters

import com.practicum.playlistmaker.search.data.dto.TrackHistoryDto
import com.practicum.playlistmaker.search.domain.models.Track

class TrackHistoryConverter {
    fun mapToTrackHistoryDto(track: Track): TrackHistoryDto {
        return TrackHistoryDto(
            track.trackId,
            track.trackName,
            track.artistName,
            track.trackTime,
            track.artworkUrl100,
            track.collectionName,
            track.releaseDate,
            track.primaryGenreName,
            track.country,
            track.previewUrl,
        )
    }

    fun mapToTrack(trackHistoryDto: TrackHistoryDto, isFavorite: Boolean): Track {
        return Track(
            trackHistoryDto.trackId,
            trackHistoryDto.trackName,
            trackHistoryDto.artistName,
            trackHistoryDto.trackTime,
            trackHistoryDto.artworkUrl100,
            trackHistoryDto.collectionName,
            trackHistoryDto.releaseDate,
            trackHistoryDto.primaryGenreName,
            trackHistoryDto.country,
            trackHistoryDto.previewUrl,
            isFavorite
        )
    }
}