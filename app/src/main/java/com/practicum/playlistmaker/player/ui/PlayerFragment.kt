package com.practicum.playlistmaker.player.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlayerBinding
import com.practicum.playlistmaker.player.domain.MediaPlayerState
import com.practicum.playlistmaker.search.domain.models.Track
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class PlayerFragment : Fragment() {
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModel()
    private val gson: Gson by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentPlayerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val track = gson.fromJson(requireArguments().getString(TRACK), Track::class.java)
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
        updateLikeButtonState(track.isFavorite)

        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.playPauseButton.setOnClickListener { viewModel.playbackControl() }
        binding.likeButton.setOnClickListener { viewModel.onLikeClicked(track) }

        viewModel.preparePlayer(track.previewUrl, track.isFavorite)

        viewModel.observeState().observe(viewLifecycleOwner) {
            if (it.mediaPlayerState != null) updateMediaPlayerState(it.mediaPlayerState!!)
            if (it.timerText != null) updateTimer(it.timerText!!)
            if (it.isFavorite != null) updateLikeButtonState(it.isFavorite!!)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateMediaPlayerState(mediaPlayerState: MediaPlayerState) {
        when (mediaPlayerState) {
            MediaPlayerState.PREPARED -> {
                binding.playPauseButton.isEnabled = true
                binding.playPauseButton.setImageResource(R.drawable.play_button)
            }

            MediaPlayerState.PLAYING -> {
                binding.playPauseButton.setImageResource(R.drawable.pause_button)
            }

            MediaPlayerState.PAUSED -> {
                binding.playPauseButton.setImageResource(R.drawable.play_button)
            }

            MediaPlayerState.DEFAULT -> {
                binding.playPauseButton.isEnabled = false
                binding.playPauseButton.setImageResource(0)
            }
        }
    }

    private fun updateTimer(timerText: String) {
        binding.playTime.text = timerText
    }

    private fun updateLikeButtonState(isFavorite: Boolean) {
        if (isFavorite) binding.likeButton.setImageResource(R.drawable.like_button)
        else binding.likeButton.setImageResource(R.drawable.unlike_button)
    }

    companion object {
        const val TRACK = "TRACK"
        private const val START_TIME = "00:00"

        fun createArgs(track: String): Bundle =
            bundleOf(TRACK to track)
    }
}