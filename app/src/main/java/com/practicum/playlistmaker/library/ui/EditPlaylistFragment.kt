package com.practicum.playlistmaker.library.ui

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.library.domain.model.Playlist
import com.practicum.playlistmaker.playlist.domain.PlaylistState
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditPlaylistFragment : CreatePlaylistFragment() {

    private val viewModel: EditPlaylistViewModel by viewModel()
    private var playlist: Playlist? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playlistId = requireArguments().getLong(PLAYLIST_ID)

        viewModel.observeState().observe(viewLifecycleOwner) {
            when(it) {
                is PlaylistState.Content -> {
                    playlist = it.playlist
                    Glide.with(requireContext())
                        .load(it.playlist.coverUri)
                        .placeholder(R.drawable.add_photo)
                        .transform(
                            CenterCrop(),
                            RoundedCorners((requireContext().resources.displayMetrics.density * 8 + 0.5f).toInt())
                        )
                        .into(binding.addPhotoImageView)

                    binding.nameEditText.text.clear()
                    binding.nameEditText.text.insert(0, it.playlist.name)

                    binding.descriptionEditText.text.clear()
                    binding.descriptionEditText.text.insert(0, it.playlist.description)
                }

                else -> {}
            }
        }
        viewModel.uploadInfo(playlistId)

        requireActivity().onBackPressedDispatcher.addCallback { findNavController().navigateUp() }
        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        binding.headerTextView.text = resources.getString(R.string.edit)

        binding.createTextView.text = resources.getString(R.string.save)

        binding.createTextView.setOnClickListener {
            val playlistName = binding.nameEditText.text.toString()
            val playListDescription = binding.descriptionEditText.text.toString()
            if (playlistName.isNotEmpty()) {
                viewModel.updatePlaylist(playlist, playlistName, playListDescription, imageUri)
                findNavController().navigateUp()
            }
        }
    }

    companion object {
        private const val PLAYLIST_ID = "PLAYLIST_ID"

        fun createArgs(playlistId: Long): Bundle =
            bundleOf(PLAYLIST_ID to playlistId)
    }
}