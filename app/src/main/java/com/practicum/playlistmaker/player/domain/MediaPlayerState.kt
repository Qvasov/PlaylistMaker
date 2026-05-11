package com.practicum.playlistmaker.player.domain

data class PlayerState(var mediaPlayerState: MediaPlayerState? = null,
                       var timerText: String? = null,
                       var isFavorite: Boolean? = null) {
}

enum class MediaPlayerState {
    PREPARED,
    PLAYING,
    PAUSED,
    DEFAULT
}