package com.practicum.playlistmaker.player.ui

import android.media.MediaPlayer
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel(
    private var mediaPlayer: MediaPlayer,
    private var handler: Handler
) : ViewModel() {

    enum class PlayerState {
        PREPARED,
        PLAYING,
        PAUSED
    }

    private var playerState: PlayerState? = null
    private var timerTask: Runnable = createTimerTask()
    private val dateFormat by lazy { SimpleDateFormat("mm:ss", Locale.getDefault()) }


    private val stateLiveData = MutableLiveData<PlayerState>()
    fun observeState(): LiveData<PlayerState> = stateLiveData

    private val timerLiveData = MutableLiveData<String>()
    fun observeTimer(): LiveData<String> = timerLiveData

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(timerTask)
        mediaPlayer.release()
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
            handler.removeCallbacks(timerTask)
            timerLiveData.postValue(START_TIME)
            playerState = PlayerState.PREPARED
            stateLiveData.postValue(PlayerState.PREPARED)
        }
    }

    fun startPlayer() {
        mediaPlayer.start()
        handler.post(timerTask)
        playerState = PlayerState.PLAYING
        stateLiveData.postValue(PlayerState.PLAYING)
    }

    fun pausePlayer() {
        mediaPlayer.pause()
        handler.removeCallbacks(timerTask)
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

    private fun createTimerTask(): Runnable {
        return object : Runnable {
            override fun run() {
                val elapsedTime = dateFormat.format(mediaPlayer.currentPosition)
                timerLiveData.postValue(elapsedTime)
                handler.postDelayed(this, TIMER_DELAY)
            }
        }
    }

    companion object {
        private const val START_TIME = "00:00"
        private const val TIMER_DELAY = 350L
    }
}