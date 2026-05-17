package com.practicum.playlistmaker.library.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val coverUri: String? = null,
    val trackIds: String,
    val tracksCount: Int
)