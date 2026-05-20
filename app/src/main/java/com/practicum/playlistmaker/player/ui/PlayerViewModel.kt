package com.practicum.playlistmaker.player.ui

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.PlaylistsState
import com.practicum.playlistmaker.library.domain.api.FavoritesInteractor
import com.practicum.playlistmaker.library.domain.api.PlaylistsInteractor
import com.practicum.playlistmaker.library.domain.model.Playlist
import com.practicum.playlistmaker.player.domain.MediaPlayerState
import com.practicum.playlistmaker.player.domain.PlayerState
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.utils.SingleLiveEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel(
    private val favoritesInteractor: FavoritesInteractor,
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private lateinit var mediaPlayer: MediaPlayer
    private var mediaPlayerState: MediaPlayerState = MediaPlayerState.DEFAULT
    private var timerJob: Job? = null
    private val dateFormat by lazy { SimpleDateFormat("mm:ss", Locale.getDefault()) }

    private val stateLiveData = MutableLiveData<PlayerState>()
    fun observeState(): LiveData<PlayerState> = stateLiveData

    private val bottomSheetStateLiveData = MutableLiveData<PlaylistsState>()
    fun observeBottomSheetState(): LiveData<PlaylistsState> = bottomSheetStateLiveData

    private val addToPlaylistLiveData = SingleLiveEvent<Boolean>()
    fun observeAddToPlaylist(): LiveData<Boolean> = addToPlaylistLiveData

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        mediaPlayer.release()
        mediaPlayerState = MediaPlayerState.DEFAULT
        stateLiveData.postValue(PlayerState(mediaPlayerState))
    }

    fun preparePlayer(previewUrl: String) {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(previewUrl)
        mediaPlayer.prepareAsync()
        mediaPlayerState = MediaPlayerState.PREPARED
        mediaPlayer.setOnPreparedListener {
            stateLiveData.postValue(PlayerState(mediaPlayerState, START_TIME))
        }

        mediaPlayer.setOnCompletionListener {
            timerJob?.cancel()
            mediaPlayerState = MediaPlayerState.PREPARED
            stateLiveData.postValue(PlayerState(mediaPlayerState, START_TIME))
        }
    }

    fun startPlayer() {
        mediaPlayer.start()
        mediaPlayerState = MediaPlayerState.PLAYING
        stateLiveData.postValue(PlayerState(mediaPlayerState))
        startTimer()
    }

    fun pausePlayer() {
        mediaPlayer.pause()
        timerJob?.cancel()
        mediaPlayerState = MediaPlayerState.PAUSED
        stateLiveData.postValue(PlayerState(mediaPlayerState))
    }

    fun playbackControl() {
        when (mediaPlayerState) {
            MediaPlayerState.PLAYING -> pausePlayer()
            MediaPlayerState.PREPARED, MediaPlayerState.PAUSED -> startPlayer()
            MediaPlayerState.DEFAULT -> stateLiveData.postValue(PlayerState(mediaPlayerState))
            else -> {}
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (mediaPlayer.isPlaying) {
                delay(TIMER_DELAY)
                val elapsedTime = dateFormat.format(mediaPlayer.currentPosition)
                stateLiveData.postValue(PlayerState(timerText = elapsedTime))
            }
        }
    }

    fun onLikeClicked(track: Track) {
        viewModelScope.launch {
            if (track.isFavorite) {
                track.isFavorite = false
                favoritesInteractor.deleteTrack(track)
            } else {
                track.isFavorite = true
                favoritesInteractor.addTrack(track)
            }
            stateLiveData.postValue(PlayerState(isFavorite = track.isFavorite))
        }
    }

    fun uploadPlaylist() {
        renderSheetState(PlaylistsState.Loading)
        viewModelScope.launch {
            playlistsInteractor.getAllPlaylists()
                .collect { foundPlaylists ->
                    if (foundPlaylists.isEmpty()) {
                        renderSheetState(PlaylistsState.Empty)
                    } else {
                        val playlists = mutableListOf<Playlist>()
                        playlists.addAll(foundPlaylists)
                        renderSheetState(PlaylistsState.Content(playlists))
                    }
                }
        }
    }

    fun addToPlaylist(track: Track, playlist: Playlist) {
        viewModelScope.launch {
            if (playlist.trackIds.contains(track.trackId)) {
                addToPlaylistLiveData.postValue(false)
            } else {
                playlistsInteractor.addToPlaylist(track, playlist)
                addToPlaylistLiveData.postValue(true)
            }

        }
    }

    private fun renderSheetState(state: PlaylistsState) {
        bottomSheetStateLiveData.postValue(state)
    }


    companion object {
        private const val START_TIME = "00:00"
        private const val TIMER_DELAY = 350L
    }
}