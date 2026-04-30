package com.practicum.playlistmaker.presentation

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.Creator
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.api.TracksInteractor
import com.practicum.playlistmaker.domain.models.Track

class SearchActivity : AppCompatActivity() {
    companion object {
        const val EDITTEXT_TEXT = "EDITTEXT_TEXT"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private val searchRequest = Runnable { search(searchEditText.text.toString()) }

    private var message: String = ""
    private var isClickAllowed = true
    private val gson = Creator.createGson()

    private lateinit var trackInteractor: TracksInteractor
    private lateinit var trackListAdapter: TrackAdapter

    private lateinit var backButton: FrameLayout
    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var trackListView: RecyclerView
    private lateinit var alertLayout: LinearLayout
    private lateinit var alertTextView: TextView
    private lateinit var alertImageView: ImageView
    private lateinit var refreshButton: Button
    private lateinit var historyAlertText: TextView
    private lateinit var historyClearButton: Button
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        backButton = findViewById(R.id.backButton)
        searchEditText = findViewById(R.id.search_edit_text)
        clearButton = findViewById(R.id.clearIcon)
        trackListView = findViewById(R.id.trackList)
        alertLayout = findViewById(R.id.alertLayout)
        alertTextView = findViewById(R.id.alertText)
        alertImageView = findViewById(R.id.alertImage)
        refreshButton = findViewById(R.id.refreshButton)
        historyAlertText = findViewById(R.id.historyAlertText)
        historyClearButton = findViewById(R.id.historyClearButton)
        progressBar = findViewById(R.id.progressBar)

        trackInteractor = Creator.provideTracksInteractor(this)
        trackListAdapter = TrackAdapter {
            if (clickDebounce()) {
                trackInteractor.saveToHistory(it)
                val playerIntent = Intent(this, PlayerActivity::class.java)
                playerIntent.putExtra(PlayerActivity.TRACK, gson.toJson(it))
                startActivity(playerIntent)
            }
        }
        trackListView.adapter = trackListAdapter


        if (savedInstanceState != null) {
            onSaveInstanceState(savedInstanceState)
        }

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        clearButton.setOnClickListener {
            searchEditText.setText("")
            val inputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(searchEditText.windowToken, 0)
            searchEditText.clearFocus()
            historyAlert(true)
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = clearButtonVisibility(s)
                message = s.toString()

                if (searchEditText.hasFocus() && s.isNullOrEmpty()) {
                    historyAlert(true)
                } else {
                    historyAlert(false)
                    searchDebounce()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        searchEditText.addTextChangedListener(simpleTextWatcher)
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handler.removeCallbacks(searchRequest)
                search(searchEditText.text.toString())
                true
            }
            false
        }
        searchEditText.requestFocus()
        searchEditText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus && searchEditText.text.isEmpty()) {
                historyAlert(true)
            }
        }

        refreshButton.setOnClickListener {
            handler.removeCallbacks(searchRequest)
            search(searchEditText.text.toString())
        }

        historyClearButton.setOnClickListener {
            trackInteractor.clearHistory()
            historyAlert(true)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        historyAlert(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(searchRequest)
    }

    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(EDITTEXT_TEXT, message)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        searchEditText.setText(savedInstanceState.getString(EDITTEXT_TEXT, message))
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRequest)
        handler.postDelayed(searchRequest, SEARCH_DEBOUNCE_DELAY)
    }

    private fun search(term: String) {
        historyAlert(false)
        showAlert(false)
        trackListView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        trackInteractor.search(term, object : TracksInteractor.TracksConsumer {
            override fun consume(foundTracks: List<Track>?) {
                handler.post { progressBar.visibility = View.GONE }
                if (foundTracks?.isNotEmpty() == true) {
                    handler.post { trackListView.visibility = View.VISIBLE }
                    trackListAdapter.trackList = foundTracks
                    handler.post { trackListAdapter.notifyDataSetChanged() }
                    showAlert(false)
                } else if (foundTracks?.isEmpty() == true) {
                    handler.post {
                        showAlert(
                            true,
                            getString(R.string.not_found),
                            getDrawable(R.drawable.not_found)
                        )
                    }
                } else {
                    handler.post {
                        showAlert(
                            true,
                            getString(R.string.connection_lost)
                                    + System.lineSeparator()
                                    + System.lineSeparator()
                                    + getString(R.string.fail_download),
                            getDrawable(R.drawable.connection_lost),
                            true
                        )
                    }
                }
            }
        })
    }

    private fun showAlert(
        show: Boolean,
        message: String? = null,
        drawable: Drawable? = null,
        refreshButtonVisible: Boolean = false
    ) {
        if (show) {
            showPlaceHolder(message, drawable, refreshButtonVisible)
            alertLayout.visibility = View.VISIBLE
        } else {
            showPlaceHolder(null, null)
            alertLayout.visibility = View.GONE
        }
    }

    private fun showPlaceHolder(
        message: String?,
        drawable: Drawable?,
        refreshButtonVisible: Boolean = false
    ) {
        if (message?.isNotEmpty() == true) {
            alertTextView.setText(message)
            alertTextView.visibility = View.VISIBLE
        } else {
            alertTextView.visibility = View.GONE
        }

        if (drawable != null) {
            alertImageView.visibility = View.VISIBLE
            Glide.with(applicationContext)
                .load(drawable)
                .centerInside()
                .into(alertImageView)
        } else {
            alertImageView.visibility = View.GONE
        }

        if (refreshButtonVisible) {
            refreshButton.visibility = View.VISIBLE
        } else {
            refreshButton.visibility = View.GONE
        }
    }

    private fun historyAlert(show: Boolean) {
        if (show) {
            trackInteractor.getHistory(object : TracksInteractor.TracksConsumer {
                override fun consume(foundTracks: List<Track>?) {
                    if (foundTracks?.isNotEmpty() == true) {
                        trackListAdapter.trackList = foundTracks
                        handler.post {
                            trackListView.visibility = View.VISIBLE
                            historyAlertText.visibility = View.VISIBLE
                            historyClearButton.visibility = View.VISIBLE
                        }
                    } else {
                        trackListAdapter.trackList = emptyList()
                        handler.post {
                            historyAlertText.visibility = View.GONE
                            historyClearButton.visibility = View.GONE
                            trackListView.visibility = View.GONE
                        }
                    }
                    handler.post {
                        trackListAdapter.notifyDataSetChanged()
                    }
                }
            })

            showAlert(false)
        } else {
            historyAlertText.visibility = View.GONE
            historyClearButton.visibility = View.GONE
            trackListView.visibility = View.GONE
        }
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }
}