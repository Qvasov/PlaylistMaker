package com.practicum.playlistmaker.player.ui

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.api.FavoritesInteractor
import com.practicum.playlistmaker.player.domain.MediaPlayerState
import com.practicum.playlistmaker.player.domain.PlayerState
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel(
    private var mediaPlayer: MediaPlayer,
    private val favoritesInteractor: FavoritesInteractor
) : ViewModel() {

    private var mediaPlayerState: MediaPlayerState? = null
    private var timerJob: Job? = null
    private val dateFormat by lazy { SimpleDateFormat("mm:ss", Locale.getDefault()) }

    private val stateLiveData = MutableLiveData<PlayerState>()
    fun observeState(): LiveData<PlayerState> = stateLiveData

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        mediaPlayer.release()
        mediaPlayerState = MediaPlayerState.DEFAULT
        stateLiveData.postValue(PlayerState(mediaPlayerState))
    }

    fun preparePlayer(previewUrl: String, isFavorite: Boolean) {
        mediaPlayer.setDataSource(previewUrl)
        mediaPlayer.prepareAsync()
        mediaPlayerState = MediaPlayerState.PREPARED
        mediaPlayer.setOnPreparedListener {
            stateLiveData.postValue(PlayerState(mediaPlayerState, START_TIME, isFavorite))
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

    companion object {
        private const val START_TIME = "00:00"
        private const val TIMER_DELAY = 350L
    }
}