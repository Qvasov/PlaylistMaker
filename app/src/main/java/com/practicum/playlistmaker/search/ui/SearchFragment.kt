package com.practicum.playlistmaker.search.ui

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentSearchBinding
import com.practicum.playlistmaker.player.ui.PlayerFragment
import com.practicum.playlistmaker.utils.debounce
import kotlinx.coroutines.Job
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModel()
    private val gson: Gson by inject()

    private var textWatcher: TextWatcher? = null
    private var isClickAllowed = true
    private lateinit var changeTrackClickStatusOn: (Boolean, Long) -> Job?
    private lateinit var trackListAdapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observeState().observe(viewLifecycleOwner) {
            render(it)
        }

        changeTrackClickStatusOn = debounce(lifecycleScope, false) { status ->
            isClickAllowed = status
        }
        trackListAdapter = TrackAdapter {
            if (isClickAllowed) {
                isClickAllowed = false
                viewModel.saveToHistory(it)
                findNavController().navigate(
                    R.id.action_searchFragment_to_playerFragment,
                    PlayerFragment.createArgs(gson.toJson(it))
                )
                changeTrackClickStatusOn(true, CLICK_DEBOUNCE_DELAY)
            }
        }
        binding.trackListView.adapter = trackListAdapter

        binding.clearIcon.setOnClickListener {
            binding.searchEditText.setText("")
            val inputMethodManager =
                requireContext().getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
            binding.searchEditText.clearFocus()
            viewModel.uploadHistory()
        }

        textWatcher = binding.searchEditText.doOnTextChanged { text, start, before, count ->
            binding.clearIcon.visibility = clearButtonVisibility(text)
            if (binding.searchEditText.hasFocus() && text.isNullOrEmpty()) {
                viewModel.uploadHistory()
            } else {
                viewModel.searchDebounce(text?.toString() ?: "")
            }
        }
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.searchInstant(binding.searchEditText.text.toString())
                true
            }
            false
        }
        binding.searchEditText.requestFocus()
        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.searchEditText.text.isEmpty()) {
                viewModel.uploadHistory()
            }
        }

        binding.refreshButton.setOnClickListener {
            viewModel.searchInstant(binding.searchEditText.text.toString())
        }

        binding.historyClearButton.setOnClickListener {
            viewModel.clearHistory()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.searchEditText.removeTextChangedListener(textWatcher)
        binding.trackListView.adapter = null
        _binding = null
    }

    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
    }


    private fun render(state: SearchState) {
        when (state) {
            is SearchState.Loading -> showLoading()
            is SearchState.Content -> showContent(state)
            is SearchState.History -> showHistory(state)
            is SearchState.Empty -> showError()
            is SearchState.Error -> showError()
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.trackListView.visibility = View.GONE
        binding.historyAlertText.visibility = View.GONE
        binding.historyClearButton.visibility = View.GONE
        binding.alertLayout.visibility = View.GONE
    }

    private fun showContent(state: SearchState.Content) {
        trackListAdapter.trackList = state.trackList
        trackListAdapter.notifyDataSetChanged()

        binding.progressBar.visibility = View.GONE
        if (state.trackList.isNotEmpty()) {
            binding.trackListView.visibility = View.VISIBLE
            binding.alertLayout.visibility = View.GONE
        } else {
            binding.trackListView.visibility = View.GONE
            binding.alertLayout.visibility = View.VISIBLE
            binding.alertText.setText(getString(R.string.not_found))
            Glide.with(requireContext())
                .load(resources.getDrawable(R.drawable.not_found, null))
                .centerInside()
                .into(binding.alertImage)
            binding.refreshButton.visibility = View.GONE
        }

        binding.historyAlertText.visibility = View.GONE
        binding.historyClearButton.visibility = View.GONE
    }

    private fun showHistory(state: SearchState.History) {
        trackListAdapter.trackList = state.trackList
        trackListAdapter.notifyDataSetChanged()

        binding.progressBar.visibility = View.GONE
        binding.alertLayout.visibility = View.GONE
        if (state.trackList.isNotEmpty()) {
            binding.trackListView.visibility = View.VISIBLE
            binding.historyAlertText.visibility = View.VISIBLE
            binding.historyClearButton.visibility = View.VISIBLE
        } else {
            binding.trackListView.visibility = View.GONE
            binding.historyAlertText.visibility = View.GONE
            binding.historyClearButton.visibility = View.GONE
        }
    }

    private fun showError() {
        binding.progressBar.visibility = View.GONE
        binding.trackListView.visibility = View.GONE

        binding.alertLayout.visibility = View.VISIBLE
        binding.alertText.setText(
            getString(R.string.connection_lost)
                    + System.lineSeparator()
                    + System.lineSeparator()
                    + getString(R.string.fail_download)
        )
        Glide.with(requireContext())
            .load(resources.getDrawable(R.drawable.connection_lost, null))
            .centerInside()
            .into(binding.alertImage)
        binding.refreshButton.visibility = View.VISIBLE

        binding.historyAlertText.visibility = View.GONE
        binding.historyClearButton.visibility = View.GONE
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}