package com.practicum.playlistmaker.search.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.creator.Creator
import com.practicum.playlistmaker.databinding.ActivitySearchBinding
import com.practicum.playlistmaker.player.ui.PlayerActivity

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding

    private var viewModel: SearchViewModel? = null
    private val gson = Creator.createGson()
    private var isClickAllowed = true
    private val handler = Handler(Looper.getMainLooper())
    private var textWatcher: TextWatcher? = null
    private var trackListAdapter = TrackAdapter {
        if (clickDebounce()) {
            viewModel?.saveToHistory(it)
            val playerIntent = Intent(this, PlayerActivity::class.java)
            playerIntent.putExtra(PlayerActivity.TRACK, gson.toJson(it))
            startActivity(playerIntent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.search) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.trackListView.adapter = trackListAdapter

        viewModel = ViewModelProvider(this, SearchViewModel.getFactory())
            .get(SearchViewModel::class.java)
        viewModel?.observeState()?.observe(this) {
            render(it)
        }

        if (savedInstanceState != null) {
            onSaveInstanceState(savedInstanceState)
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.clearIcon.setOnClickListener {
            binding.searchEditText.setText("")
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
            binding.searchEditText.clearFocus()
            viewModel?.uploadHistory()
        }

        binding.searchEditText.doOnTextChanged { text, start, before, count ->
            binding.clearIcon.visibility = clearButtonVisibility(text)
            if (binding.searchEditText.hasFocus() && text.isNullOrEmpty()) {
                viewModel?.uploadHistory()
            } else {
                viewModel?.searchDebounce(text?.toString() ?: "")
            }
        }
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel?.searchInstant(binding.searchEditText.text.toString())
                true
            }
            false
        }
        binding.searchEditText.requestFocus()
        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.searchEditText.text.isEmpty()) {
                viewModel?.uploadHistory()
            }
        }

        binding.refreshButton.setOnClickListener {
            viewModel?.searchInstant(binding.searchEditText.text.toString())
        }

        binding.historyClearButton.setOnClickListener {
            viewModel?.clearHistory()
        }


        viewModel?.uploadHistory()
    }

    override fun onDestroy() {
        super.onDestroy()
        textWatcher?.let { binding.searchEditText.removeTextChangedListener(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_EDITTEXT_TEXT, binding.searchEditText.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        binding.searchEditText.setText(
            savedInstanceState.getString(
                SEARCH_EDITTEXT_TEXT,
                binding.searchEditText.text.toString()
            )
        )
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
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
            Glide.with(applicationContext)
                .load(getDrawable(R.drawable.not_found))
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
        Glide.with(applicationContext)
            .load(getDrawable(R.drawable.connection_lost))
            .centerInside()
            .into(binding.alertImage)
        binding.refreshButton.visibility = View.VISIBLE

        binding.historyAlertText.visibility = View.GONE
        binding.historyClearButton.visibility = View.GONE
    }

    companion object {
        const val SEARCH_EDITTEXT_TEXT = "SEARCH_EDITTEXT_TEXT"
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}