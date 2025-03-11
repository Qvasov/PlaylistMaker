package com.practicum.playlistmaker

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
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
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.api.Track
import com.practicum.playlistmaker.api.iTunesApiResponse
import com.practicum.playlistmaker.searchview.SearchHistoryService
import com.practicum.playlistmaker.searchview.TrackAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {
    companion object {
        const val EDITTEXT_TEXT = "EDITTEXT_TEXT"
        private const val SEARCH_ACTIVITY = "search_activity"
    }

    private val iTunesApiController = RetrofitService.createITunesController()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var searchHistoryService: SearchHistoryService

    private var message: String = ""
    private val trackList = ArrayList<Track>()

    private lateinit var trackListAdapter: TrackAdapter
    private lateinit var trackListHistoryAdapter: TrackAdapter

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        sharedPreferences = getSharedPreferences(SEARCH_ACTIVITY, MODE_PRIVATE)
        searchHistoryService = SearchHistoryService(sharedPreferences)

        trackListAdapter = TrackAdapter(trackList, searchHistoryService)
        trackListHistoryAdapter = TrackAdapter(
            searchHistoryService.getTrackHistory(), searchHistoryService
        )

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

        if (savedInstanceState != null) {
            onSaveInstanceState(savedInstanceState)
        }

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        clearButton.setOnClickListener {
            searchEditText.setText("")
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(searchEditText.windowToken, 0)
            searchEditText.clearFocus()
            trackList.clear()
            trackListAdapter.notifyDataSetChanged()
            showAlert(false)
            historyAlert(true)
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = clearButtonVisibility(s)
                if (s.isNullOrEmpty()) {
                    if (trackList.isEmpty()) {
                        showAlert(false)
                        historyAlert(true)
                    } else {
                        trackList.clear()
                        if (searchHistoryService.getTrackHistory().isNotEmpty()) {
                            historyAlert(true)
                        }
                    }
                }
                message = s.toString()

                if (searchEditText.hasFocus() && s.isNullOrEmpty()) {
                    historyAlert(true)
                } else {
                    historyAlert(false)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        searchEditText.addTextChangedListener(simpleTextWatcher)
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
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

        historyAlert(true)

        refreshButton.setOnClickListener { search(searchEditText.text.toString()) }

        historyClearButton.setOnClickListener {
            searchHistoryService.clearTrackHistory()
            historyAlert(true)
        }

        showAlert(false)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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

    private fun search(term: String) {
        iTunesApiController.search(term)
            .enqueue(object : Callback<iTunesApiResponse> {
                override fun onResponse(
                    call: Call<iTunesApiResponse>,
                    response: Response<iTunesApiResponse>
                ) {
                    if (response.code() == 200) {
                        if (response.body()?.results?.isNotEmpty() == true) {
                            trackList.clear()
                            trackList.addAll(response.body()?.results!!)
                            trackListAdapter.notifyDataSetChanged()
                            showAlert(false)
                        } else {
                            showAlert(
                                true,
                                getString(R.string.not_found),
                                getDrawable(R.drawable.not_found)
                            )
                        }
                    } else if (response.code() == 404) {
                        showAlert(
                            true,
                            getString(R.string.not_found),
                            getDrawable(R.drawable.not_found)
                        )
                    } else {
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

                override fun onFailure(call: Call<iTunesApiResponse>, t: Throwable) {
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
            trackList.clear()
            trackListAdapter.notifyDataSetChanged()
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
        if (show && searchHistoryService.getTrackHistory().isNotEmpty()) {
            historyAlertText.visibility = View.VISIBLE
            historyClearButton.visibility = View.VISIBLE
            trackListView.adapter =
                TrackAdapter(searchHistoryService.getTrackHistory(), searchHistoryService)
        } else {
            historyAlertText.visibility = View.GONE
            historyClearButton.visibility = View.GONE
            trackListView.adapter = trackListAdapter
        }
        trackListHistoryAdapter.notifyDataSetChanged()
    }
}