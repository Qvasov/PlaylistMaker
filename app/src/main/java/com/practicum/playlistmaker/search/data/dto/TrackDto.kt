package com.practicum.playlistmaker.search.data.dto

import android.util.Log
import com.google.gson.annotations.SerializedName
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class TrackDto(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    @SerializedName("trackTimeMillis")
    val trackTime: Long,
    val artworkUrl100: String,
    val collectionName: String,
    val releaseDate: String?,
    val primaryGenreName: String,
    val country: String,
    val previewUrl: String
) {
    fun getCoverArtwork() = artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")

    fun getReleaseYear(): String {
        try {
            if (releaseDate != null) {
                return LocalDateTime.ofInstant(
                    Instant.parse(releaseDate),
                    ZoneId.systemDefault()
                ).year.toString()
            }
        } catch (e: DateTimeException) {
            Log.e("Track", "Error parse releaseDate", e)
        }
        return ""
    }
}

