package com.practicum.playlistmaker.library.domain.model

import android.net.Uri

data class Playlist(
    val id: Long? = null,
    val name: String,
    val description: String?,
    val coverUri: Uri?,
    val trackIds: List<Long>,
    val tracksCount: Int
)