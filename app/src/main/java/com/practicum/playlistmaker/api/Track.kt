package com.practicum.playlistmaker.api

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

data class Track(
    val trackId: Int,
    val trackName: String,
    val artistName: String,
    @SerializedName("trackTimeMillis")
    val trackTime: Long,
    val artworkUrl100: String,
    val collectionName: String,
    val releaseDate: String,
    val primaryGenreName: String,
    val country: String
) {
    fun getSimpleTrackTime() =
        SimpleDateFormat("mm:ss", Locale.getDefault()).format(trackTime).toString()

    fun getCoverArtwork() = artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")

    fun getReleaseYear() =
        LocalDateTime.ofInstant(Instant.parse(releaseDate), ZoneId.systemDefault()).year.toString()
}