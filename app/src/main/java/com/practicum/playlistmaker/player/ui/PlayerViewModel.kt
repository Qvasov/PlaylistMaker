package com.practicum.playlistmaker.player.ui

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.player.domain.PlayerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel(
    private var mediaPlayer: MediaPlayer
) : ViewModel() {

    private var playerState: PlayerState? = null
    private var timerJob: Job? = null
    private val dateFormat by lazy { SimpleDateFormat("mm:ss", Locale.getDefault()) }


    private val stateLiveData = MutableLiveData<PlayerState>()
    fun observeState(): LiveData<PlayerState> = stateLiveData

    private val timerLiveData = MutableLiveData<String>()
    fun observeTimer(): LiveData<String> = timerLiveData

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

    private fun startTimer( ) {
        timerJob = viewModelScope.launch {
            while(mediaPlayer.isPlaying) {
                delay(TIMER_DELAY)
                val elapsedTime = dateFormat.format(mediaPlayer.currentPosition)
                timerLiveData.postValue(elapsedTime)
            }
        }
    }

    companion object {
        private const val START_TIME = "00:00"
        private const val TIMER_DELAY = 350L
    }
}