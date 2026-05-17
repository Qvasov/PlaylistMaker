package com.practicum.playlistmaker.library.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlaylistsBinding
import com.practicum.playlistmaker.library.domain.PlaylistsState
import com.practicum.playlistmaker.playlist.ui.PlaylistFragment
import com.practicum.playlistmaker.utils.debounce
import kotlinx.coroutines.Job
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {
    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistsViewModel by viewModel()

    private var isClickAllowed = true
    private lateinit var debounce: (Boolean, Long) -> Job?
    private lateinit var playlistsAdapter: PlaylistsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.newPlaylistButton.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_createPlaylist)
        }


        debounce = debounce(lifecycleScope, false) { status ->
            isClickAllowed = status
        }
        playlistsAdapter = PlaylistsAdapter {
            if (isClickAllowed) {
                isClickAllowed = false
                debounce(true, CLICK_DEBOUNCE_DELAY)
                findNavController().navigate(
                    R.id.action_libraryFragment_to_playlistFragment,
                    PlaylistFragment.createArgs(it.id!!)
                )
            }
        }
        binding.playlistRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.playlistRecyclerView.adapter = playlistsAdapter


        viewModel.observeState().observe(viewLifecycleOwner) { render(it) }
        viewModel.uploadPlaylist()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        binding.playlistRecyclerView.visibility = View.GONE
        binding.newPlaylistButton.visibility = View.GONE
        binding.alertLayout.visibility = View.GONE
    }

    private fun showContent(state: PlaylistsState.Content) {
        playlistsAdapter.playlists = state.playlists
        playlistsAdapter.notifyDataSetChanged()

        binding.progressBar.visibility = View.GONE
        binding.playlistRecyclerView.visibility = View.VISIBLE
        binding.newPlaylistButton.visibility = View.VISIBLE
        binding.alertLayout.visibility = View.GONE
    }

    private fun showEmpty() {
        playlistsAdapter.playlists = emptyList()
        playlistsAdapter.notifyDataSetChanged()

        binding.progressBar.visibility = View.GONE
        binding.playlistRecyclerView.visibility = View.GONE
        binding.newPlaylistButton.visibility = View.VISIBLE
        binding.alertLayout.visibility = View.VISIBLE
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L

        fun newInstance() = PlaylistsFragment()
    }
}