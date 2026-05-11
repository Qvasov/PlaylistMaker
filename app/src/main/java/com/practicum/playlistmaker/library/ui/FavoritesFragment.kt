package com.practicum.playlistmaker.library.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentFavoriteBinding
import com.practicum.playlistmaker.library.domain.LibraryState
import com.practicum.playlistmaker.player.ui.PlayerFragment
import com.practicum.playlistmaker.utils.debounce
import kotlinx.coroutines.Job
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {

    private val viewModel: FavoritesViewModel by viewModel()
    private lateinit var trackListAdapter: TrackAdapter
    private var isClickAllowed = true
    private lateinit var changeTrackClickStatusOn: (Boolean, Long) -> Job?
    private val gson: Gson by inject()

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeTrackClickStatusOn = debounce(lifecycleScope, false) { status ->
            isClickAllowed = status
        }
        trackListAdapter = TrackAdapter {
            if (isClickAllowed) {
                isClickAllowed = false
                findNavController().navigate(
                    R.id.action_libraryFragment_to_playerFragment,
                    PlayerFragment.createArgs(gson.toJson(it))
                )
                changeTrackClickStatusOn(true, CLICK_DEBOUNCE_DELAY)
            }
        }
        binding.favoriteTrackListView.adapter = trackListAdapter

        viewModel.observeState().observe(viewLifecycleOwner) {
            render(it)
        }

        viewModel.uploadFavorites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.favoriteTrackListView.adapter = null
        _binding = null
    }

    private fun render(state: LibraryState) {
        when (state) {
            is LibraryState.Loading -> showLoading()
            is LibraryState.Content -> showContent(state)
            is LibraryState.Empty -> showEmpty()
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.favoriteTrackListView.visibility = View.GONE
        binding.alertLayout.visibility = View.GONE
    }

    private fun showContent(state: LibraryState.Content) {
        trackListAdapter.trackList = state.trackList
        trackListAdapter.notifyDataSetChanged()

        binding.progressBar.visibility = View.GONE
        binding.favoriteTrackListView.visibility = View.VISIBLE
        binding.alertLayout.visibility = View.GONE
    }

    private fun showEmpty() {
        trackListAdapter.trackList = emptyList()
        trackListAdapter.notifyDataSetChanged()

        binding.progressBar.visibility = View.GONE
        binding.favoriteTrackListView.visibility = View.GONE
        binding.alertLayout.visibility = View.VISIBLE
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        fun newInstance() = FavoritesFragment()
    }
}