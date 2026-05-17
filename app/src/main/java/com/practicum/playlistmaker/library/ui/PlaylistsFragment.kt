package com.practicum.playlistmaker.library.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlaylistsBinding
import com.practicum.playlistmaker.library.domain.PlaylistsState
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {
    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistsViewModel by viewModel()

    private lateinit var playlistAdapter: PlaylistAdapter

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


        playlistAdapter = PlaylistAdapter {

        }
        binding.playlistRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.playlistRecyclerView.adapter = playlistAdapter


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
        playlistAdapter.playlists = state.playlists
        playlistAdapter.notifyDataSetChanged()

        binding.progressBar.visibility = View.GONE
        binding.playlistRecyclerView.visibility = View.VISIBLE
        binding.newPlaylistButton.visibility = View.VISIBLE
        binding.alertLayout.visibility = View.GONE
    }

    private fun showEmpty() {
        playlistAdapter.playlists = emptyList()
        playlistAdapter.notifyDataSetChanged()

        binding.progressBar.visibility = View.GONE
        binding.playlistRecyclerView.visibility = View.GONE
        binding.newPlaylistButton.visibility = View.VISIBLE
        binding.alertLayout.visibility = View.VISIBLE
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}