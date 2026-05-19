package com.practicum.playlistmaker.library.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_track")
data class PlaylistTrackEntity(
    @PrimaryKey
    val trackId: Long,
    val artworkUrl100: String,
    val trackName: String,
    val artistName: String,
    val collectionName: String,
    val releaseDate: String,
    val country: String,
    val trackTime: Long,
    val primaryGenreName: String,
    val previewUrl: String,
    val addTimestamp: Long
)