package com.practicum.playlistmaker.player.ui

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.api.FavoritesInteractor
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

    private var playerState: PlayerState? = null
    private var timerJob: Job? = null
    private val dateFormat by lazy { SimpleDateFormat("mm:ss", Locale.getDefault()) }


    private val stateLiveData = MutableLiveData<PlayerState>()
    fun observeState(): LiveData<PlayerState> = stateLiveData

    private val timerLiveData = MutableLiveData<String>()
    fun observeTimer(): LiveData<String> = timerLiveData

    private val likeButtonData = MutableLiveData<Boolean>()
    fun observeLikeButton(): LiveData<Boolean> = likeButtonData

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        mediaPlayer.release()
        stateLiveData.postValue(PlayerState.DEFAULT)
    }

    fun preparePlayer(previewUrl: String) {
        mediaPlayer.setDataSource(previewUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            timerLiveData.postValue(START_TIME)
            playerState = PlayerState.PREPARED
            stateLiveData.postValue(PlayerState.PREPARED)
        }
        mediaPlayer.setOnCompletionListener {
            timerJob?.cancel()
            timerLiveData.postValue(START_TIME)
            playerState = PlayerState.PREPARED
            stateLiveData.postValue(PlayerState.PREPARED)
        }
    }

    fun startPlayer() {
        mediaPlayer.start()
        startTimer()
        playerState = PlayerState.PLAYING
        stateLiveData.postValue(PlayerState.PLAYING)
    }

    fun pausePlayer() {
        mediaPlayer.pause()
        timerJob?.cancel()
        playerState = PlayerState.PAUSED
        stateLiveData.postValue(PlayerState.PAUSED)
    }

    fun playbackControl() {
        when (playerState) {
            PlayerState.PLAYING -> pausePlayer()
            PlayerState.PREPARED, PlayerState.PAUSED -> startPlayer()
            else -> {}
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (mediaPlayer.isPlaying) {
                delay(TIMER_DELAY)
                val elapsedTime = dateFormat.format(mediaPlayer.currentPosition)
                timerLiveData.postValue(elapsedTime)
            }
        }
    }

    fun onLikeClicked(track: Track) {
        viewModelScope.launch {
            if (track.isFavorite) {
                track.isFavorite = false
                favoritesInteractor.deleteTrack(track)
                likeButtonData.postValue( track.isFavorite)
            } else {
                track.isFavorite = true
                favoritesInteractor.addTrack(track)
                likeButtonData.postValue(track.isFavorite)
            }
        }
    }

    companion object {
        private const val START_TIME = "00:00"
        private const val TIMER_DELAY = 350L
    }
}