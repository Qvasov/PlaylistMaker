package com.practicum.playlistmaker.playlist.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlaylistBinding
import com.practicum.playlistmaker.library.domain.model.Playlist
import com.practicum.playlistmaker.library.ui.EditPlaylistFragment
import com.practicum.playlistmaker.library.ui.TrackAdapter
import com.practicum.playlistmaker.player.ui.PlayerFragment
import com.practicum.playlistmaker.playlist.domain.PlaylistState
import com.practicum.playlistmaker.playlist.domain.TrackListSheetState
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.utils.debounce
import com.practicum.playlistmaker.utils.getMinutesWord
import com.practicum.playlistmaker.utils.getTrackWord
import kotlinx.coroutines.Job
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistFragment : Fragment() {
    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistViewModel by viewModel()
    private val gson: Gson by inject()

    private var isClickAllowed = true
    private lateinit var debounce: (Boolean, Long) -> Job?
    private lateinit var trackAdapter: TrackAdapter
    private var alphaOverlay: Float = 0f
    private var playlist: Playlist? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val playlistId = requireArguments().getLong(PLAYLIST_ID)

        viewModel.observeState().observe(viewLifecycleOwner) {
            when (it) {
                is PlaylistState.Loading -> {}
                is PlaylistState.Content -> showContent(it)
                is PlaylistState.Empty -> {}
            }
        }

        viewModel.uploadPlaylist(playlistId)


        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        requireActivity().onBackPressedDispatcher.addCallback { findNavController().navigateUp() }


        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        binding.shareButton.doOnLayout {
            bottomSheetBehavior.peekHeight =
                binding.root.height - it.bottom - binding.playlistTime.marginTop
        }


        val deleteTrackDialog = DeleteTrackDialog(requireContext())
        debounce = debounce(lifecycleScope, false) { status ->
            isClickAllowed = status
        }
        trackAdapter = TrackAdapter(object : TrackAdapter.TrackClickListener {
            override fun onTrackClick(track: Track) {
                if (isClickAllowed) {
                    isClickAllowed = false
                    debounce(true, CLICK_DEBOUNCE_DELAY)
                    findNavController().navigate(
                        R.id.action_playlistFragment_to_playerFragment,
                        PlayerFragment.createArgs(gson.toJson(track))
                    )
                }
            }

            override fun onTrackLongClick(track: Track): Boolean {
                deleteTrackDialog.showAndDo {
                    if (playlist != null) {
                        viewModel.deleteTrackFromPlaylist(track, playlist!!)
                    }
                }
                return true
            }
        })

        binding.playlistSheetRecyclerView.adapter = trackAdapter


        val optionsBottomSheetBehavior = BottomSheetBehavior.from(binding.optionsBottomSheet)
        optionsBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        if (optionsBottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            binding.overlay.visibility = View.VISIBLE
            binding.overlay.alpha = alphaOverlay
        }
        optionsBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        bottomSheetBehavior.isDraggable = true
                        binding.overlay.visibility = View.GONE
                    }
                    else -> {
                        bottomSheetBehavior.isDraggable = false
                        binding.overlay.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (_binding != null) {
                    alphaOverlay = binding.overlay.alpha
                    binding.overlay.alpha = (slideOffset + 1) / 2
                }
            }
        })

        binding.optionsButton.setOnClickListener {
            optionsBottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }


        val shareClick = View.OnClickListener {
            if (playlist?.tracksCount != 0) {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, getShareMessage())
                startActivity(Intent.createChooser(shareIntent, null))
            } else {
                Toast.makeText(requireContext(), NOTHING_SHARE, Toast.LENGTH_SHORT).show()
            }
        }
        binding.shareButton.setOnClickListener(shareClick)
        binding.share.setOnClickListener(shareClick)


        binding.editInfo.setOnClickListener {
            findNavController().navigate(
                R.id.action_playlistFragment_to_editPlaylistFragment,
                EditPlaylistFragment.createArgs(playlist?.id!!)
            )
        }


        binding.deletePlaylist.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage("$DELETE_PLAYLIST_QUESTION «${playlist?.name}»?")
                .setNegativeButton(NO) { dialog, which -> }
                .setPositiveButton(YES) { dialog, which ->
                    if (playlist != null) {
                        viewModel.deletePlaylist(playlist!!)
                    }
                    findNavController().navigateUp()
                }
                .show()
        }


        viewModel.observeBottomSheetState().observe(viewLifecycleOwner) { state ->
            when (state) {
                is TrackListSheetState.Loading -> showSheetLoading()
                is TrackListSheetState.Content -> showSheetContent(state)
                is TrackListSheetState.Empty -> showSheetEmpty()
            }
        }
    }

    private fun showContent(state: PlaylistState.Content) {
        playlist = state.playlist
        viewModel.uploadTracks(state.playlist.id!!)

        Glide.with(this)
            .load(state.playlist.coverUri)
            .placeholder(R.drawable.playlist_view_placeholder)
            .transform(
                CenterCrop(),
                RoundedCorners((resources.displayMetrics.density * 8 + 0.5f).toInt())
            )
            .into(binding.coverArtwork)
        binding.name.text = state.playlist.name
        binding.description.text = state.playlist.description


        Glide.with(this)
            .load(state.playlist.coverUri)
            .placeholder(R.drawable.playlist_view_placeholder)
            .transform(
                CenterCrop(),
                RoundedCorners((resources.displayMetrics.density * 2 + 0.5f).toInt())
            )
            .into(binding.playListInfo.sheetPlaylistCover)
        binding.playListInfo.sheetPlaylistTitle.text = state.playlist.name
        binding.playListInfo.sheetPlaylistTracksCount.text =
            getTrackWord(state.playlist.tracksCount)
    }

    private fun showSheetLoading() {
        binding.progressBarSheet.visibility = View.VISIBLE
        binding.sheetAlert.visibility = View.GONE
        binding.playlistSheetRecyclerView.visibility = View.GONE
    }

    private fun showSheetContent(state: TrackListSheetState.Content) {
        trackAdapter.trackList = state.trackList
        trackAdapter.notifyDataSetChanged()

        binding.playlistTime.text = getMinutesWord(state.getTrackListTime().toInt())
        binding.trackCount.text = getTrackWord(state.trackList.size)
        binding.progressBarSheet.visibility = View.GONE
        binding.sheetAlert.visibility = View.GONE
        binding.playlistSheetRecyclerView.visibility = View.VISIBLE
    }

    private fun showSheetEmpty() {
        trackAdapter.trackList = emptyList()
        trackAdapter.notifyDataSetChanged()

        binding.playlistTime.text = getMinutesWord(0)
        binding.trackCount.text = getTrackWord(0)
        binding.progressBarSheet.visibility = View.GONE
        binding.sheetAlert.visibility = View.VISIBLE
        binding.playlistSheetRecyclerView.visibility = View.GONE
    }

    private fun getShareMessage(): String {
        return """
            |${playlist?.name}
            |${playlist?.description}
            |${playlist?.tracksCount?.let { getTrackWord(it) }}
            |${
            trackAdapter.trackList.mapIndexed { index, track ->
                "${index + 1}. ${track.artistName} - ${track.trackName} (${track.getSimpleTrackTime()})"
            }.joinToString(System.lineSeparator())
        }
        """.trimMargin()


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class DeleteTrackDialog(context: Context) {
        private lateinit var action: () -> Unit
        private val deleteTrackDialog = MaterialAlertDialogBuilder(context)
            .setMessage(DELETE_TRACK_QUESTION)
            .setNegativeButton(NO) { dialog, which -> }
            .setPositiveButton(YES) { dialog, which -> action() }

        fun showAndDo(action: () -> Unit) {
            this@DeleteTrackDialog.action = action
            deleteTrackDialog.show()
        }

    }

    companion object {
        private const val PLAYLIST_ID = "PLAYLIST_ID"
        private const val CLICK_DEBOUNCE_DELAY = 1000L

        private const val DELETE_PLAYLIST_QUESTION = "Хотите удалить плейлист"
        private const val DELETE_TRACK_QUESTION = "Хотите удалить трек?"
        private const val YES = "Да"
        private const val NO = "Нет"

        private const val NOTHING_SHARE =
            "В этом плейлисте нет списка треков, которым можно поделиться"

        fun createArgs(playlist: Long): Bundle =
            bundleOf(PLAYLIST_ID to playlist)
    }

}