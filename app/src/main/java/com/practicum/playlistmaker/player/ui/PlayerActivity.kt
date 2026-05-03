package com.practicum.playlistmaker.player.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivityPlayerBinding
import com.practicum.playlistmaker.search.domain.models.Track
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private val viewModel: PlayerViewModel by viewModel()
    private val gson: Gson by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel.observeState().observe(this) {
            when (it) {
                PlayerViewModel.PlayerState.PREPARED -> {
                    binding.playPauseButton.isEnabled = true
                    binding.playPauseButton.setImageResource(R.drawable.play_button)
                }

                PlayerViewModel.PlayerState.PLAYING -> {
                    binding.playPauseButton.setImageResource(R.drawable.pause_button)
                }

                PlayerViewModel.PlayerState.PAUSED -> {
                    binding.playPauseButton.setImageResource(R.drawable.play_button)
                }

                else -> {}
            }
        }
        viewModel.observeTimer().observe(this) {
            binding.playTime.text = it
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.playPauseButton.setOnClickListener {
            viewModel.playbackControl()
        }

        val track = gson.fromJson(intent.getStringExtra(TRACK), Track::class.java)
        Glide.with(this)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.album_place_holder)
            .fitCenter()
            .transform(RoundedCorners((resources.displayMetrics.density * 8 + 0.5f).toInt()))
            .into(binding.coverArtwork)
        binding.trackName.text = track.trackName
        binding.artistName.text = track.artistName
        binding.playTime.text = START_TIME
        binding.trackTime.text = track.trackTime
        binding.collectionName.text = track.collectionName
        binding.releaseDate.text = track.releaseDate
        binding.primaryGenreName.text = track.primaryGenreName
        binding.country.text = track.country

        viewModel.preparePlayer(track.previewUrl)
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayer()
    }

    companion object {
        const val TRACK = "TRACK"
        private const val START_TIME = "00:00"
    }
}