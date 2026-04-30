package com.practicum.playlistmaker.presentation

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.Creator
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.Runnable
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {
    companion object {
        enum class PlayerState {
            DEFAULT,
            PREPARED,
            PLAYING,
            PAUSED
        }
        const val TRACK = "TRACK"
        private const val START_TIME = "00:00"
        private const val TIMER_DELAY = 350L
    }

    private val gson = Creator.createGson()
    private var mediaPlayer = MediaPlayer()
    private var playerState = PlayerState.DEFAULT
    private var handler: Handler? = null
    private var timerTask: Runnable = createTimerTask()
    private lateinit var track: Track

    private lateinit var backButton: ImageView
    private lateinit var playPauseButton: ImageView
    private lateinit var coverArtwork: ImageView
    private lateinit var trackName: TextView
    private lateinit var artistName: TextView
    private lateinit var playTime: TextView
    private lateinit var trackTime: TextView
    private lateinit var collectionName: TextView
    private lateinit var releaseDate: TextView
    private lateinit var primaryGenreName: TextView
    private lateinit var country: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)

        handler = Handler(Looper.getMainLooper())

        track = gson.fromJson(intent.getStringExtra(TRACK), Track::class.java)

        backButton = findViewById(R.id.backButton)
        playPauseButton = findViewById(R.id.playPauseButton)
        coverArtwork = findViewById(R.id.coverArtwork)
        trackName = findViewById(R.id.trackName)
        playTime = findViewById(R.id.playTime)
        artistName = findViewById(R.id.artistName)
        trackTime = findViewById(R.id.trackTime)
        collectionName = findViewById(R.id.collectionName)
        releaseDate = findViewById(R.id.releaseDate)
        primaryGenreName = findViewById(R.id.primaryGenreName)
        country = findViewById(R.id.country)

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        preparePlayer()
        playPauseButton.setOnClickListener {
            playbackControl()
        }

        Glide.with(this)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.album_place_holder)
            .fitCenter()
            .transform(RoundedCorners(8))
            .into(coverArtwork)
        trackName.text = track.trackName
        artistName.text = track.artistName
        playTime.text = START_TIME
        trackTime.text = track.trackTime
        collectionName.text = track.collectionName
        releaseDate.text = track.releaseDate
        primaryGenreName.text = track.primaryGenreName
        country.text = track.country

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.player)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun preparePlayer() {
        mediaPlayer.setDataSource(track.previewUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            playPauseButton.isEnabled = true
            playerState = PlayerState.PREPARED
        }
        mediaPlayer.setOnCompletionListener {
            playPauseButton.setImageResource(R.drawable.play_button)
            handler?.removeCallbacks(timerTask)
            playTime.text = START_TIME
            playerState = PlayerState.PREPARED
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        playPauseButton.setImageResource(R.drawable.pause_button)
        handler?.post(timerTask)
        playerState = PlayerState.PLAYING
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        playPauseButton.setImageResource(R.drawable.play_button)
        handler?.removeCallbacks(timerTask)
        playerState = PlayerState.PAUSED
    }

    private fun playbackControl() {
        when (playerState) {
            PlayerState.PLAYING -> {
                pausePlayer()
            }
            PlayerState.PREPARED, PlayerState.PAUSED -> {
                startPlayer()
            }
            else -> {}
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacks(timerTask)
        mediaPlayer.release()
    }

    private fun createTimerTask(): Runnable {
        return object : Runnable {
            override fun run() {
                val elapsedTime = SimpleDateFormat(
                    "mm:ss",
                    Locale.getDefault()
                ).format(mediaPlayer.currentPosition)

                playTime.text = elapsedTime.toString()
                handler?.postDelayed(this, TIMER_DELAY)
            }
        }
    }

}