package com.practicum.playlistmaker.player.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlayerBinding
import com.practicum.playlistmaker.library.domain.PlaylistsState
import com.practicum.playlistmaker.player.domain.MediaPlayerState
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.utils.debounce
import kotlinx.coroutines.Job
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class PlayerFragment : Fragment() {
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModel()
    private val gson: Gson by inject()

    private var isClickAllowed = true
    private lateinit var debounce: (Boolean, Long) -> Job?
    private lateinit var playlistSheetAdapter: PlaylistSheetAdapter
    private var alphaOverlay: Float = 0f
    private var playlistName: String? = null

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
        binding.trackTime.text = track.getSimpleTrackTime()
        binding.collectionName.text = track.collectionName
        binding.releaseDate.text = track.releaseDate
        binding.primaryGenreName.text = track.primaryGenreName
        binding.country.text = track.country
        updateLikeButtonState(track.isFavorite)


        debounce = debounce(lifecycleScope, false) { status ->
            isClickAllowed = status
        }
        playlistSheetAdapter = PlaylistSheetAdapter { playlist ->
            if (isClickAllowed) {
                isClickAllowed = false
                debounce(true, CLICK_DEBOUNCE_DELAY)
                viewModel.addToPlaylist(track, playlist)
                playlistName = playlist.name
            }
        }
        binding.playlistSheetRecyclerView.adapter = playlistSheetAdapter


        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            binding.overlay.visibility = View.VISIBLE
            binding.overlay.alpha = alphaOverlay
        }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> binding.overlay.visibility = View.GONE
                    else -> binding.overlay.visibility = View.VISIBLE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (_binding != null) {
                    binding.overlay.alpha = (slideOffset + 1) / 2
                    alphaOverlay = binding.overlay.alpha
                }
            }
        })


        requireActivity().onBackPressedDispatcher.addCallback { findNavController().navigateUp() }
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.addToPlaylistButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            viewModel.uploadPlaylist()
        }
        binding.playPauseButton.setOnClickListener { viewModel.playbackControl() }
        binding.likeButton.setOnClickListener { viewModel.onLikeClicked(track) }

        binding.newPlaylistButton.setOnClickListener {
            findNavController().navigate(R.id.action_playerFragment_to_createPlaylistFragment)
        }

        viewModel.preparePlayer(track.previewUrl)

        viewModel.observeState().observe(viewLifecycleOwner) {
            if (it.mediaPlayerState != null) updateMediaPlayerState(it.mediaPlayerState!!)
            if (it.timerText != null) updateTimer(it.timerText!!)
            if (it.isFavorite != null) updateLikeButtonState(it.isFavorite!!)
        }

        viewModel.observeBottomSheetState().observe(viewLifecycleOwner) { render(it) }

        viewModel.observeAddToPlaylist().observe(viewLifecycleOwner) { isAdded ->
            val message: String?
            if (isAdded) {
                message = ADDED_TO_PLAYLIST
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            } else {
                message = ALREADY_ADDED_TO_PLAYLIST
            }
            Toast.makeText(requireContext(), "$message $playlistName", Toast.LENGTH_SHORT).show()
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

    private fun render(state: PlaylistsState) {
        when (state) {
            is PlaylistsState.Loading -> showLoading()
            is PlaylistsState.Content -> showContent(state)
            is PlaylistsState.Empty -> showEmpty()
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.newPlaylistButton.visibility = View.GONE
        binding.playlistSheetRecyclerView.visibility = View.GONE
    }

    private fun showContent(state: PlaylistsState.Content) {
        playlistSheetAdapter.playlists = state.playlists
        playlistSheetAdapter.notifyDataSetChanged()

        binding.progressBar.visibility = View.GONE
        binding.newPlaylistButton.visibility = View.VISIBLE
        binding.playlistSheetRecyclerView.visibility = View.VISIBLE
    }

    private fun showEmpty() {
        playlistSheetAdapter.playlists = emptyList()
        playlistSheetAdapter.notifyDataSetChanged()

        binding.progressBar.visibility = View.GONE
        binding.newPlaylistButton.visibility = View.VISIBLE
        binding.playlistSheetRecyclerView.visibility = View.GONE
    }

    companion object {
        const val TRACK = "TRACK"
        private const val START_TIME = "00:00"
        private const val CLICK_DEBOUNCE_DELAY = 1000L

        private const val ALREADY_ADDED_TO_PLAYLIST = "Трек уже добавлен в плейлист"
        private const val ADDED_TO_PLAYLIST = "Добавлено в плейлист"

        fun createArgs(track: String): Bundle =
            bundleOf(TRACK to track)
    }
}