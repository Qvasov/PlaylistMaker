package com.practicum.playlistmaker.search.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.practicum.playlistmaker.App
import com.practicum.playlistmaker.creator.Creator
import com.practicum.playlistmaker.search.domain.api.TracksInteractor
import com.practicum.playlistmaker.search.domain.models.Track

class SearchViewModel(private val context: Context) : ViewModel() {
    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private val SEARCH_REQUEST_TOKEN = Any()

        fun getFactory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY] as App)
                SearchViewModel(app)
            }
        }
    }

    private var trackInteractor = Creator.provideTracksInteractor(context)
    private val handler = Handler(Looper.getMainLooper())
    private var lastSearchText: String? = null

    private val stateLiveData = MutableLiveData<SearchState>()

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacksAndMessages(SEARCH_REQUEST_TOKEN)
    }

    fun observeState(): LiveData<SearchState> = stateLiveData

    fun searchDebounce(searchText: String) {
        if (lastSearchText == searchText) {
            return
        }
        searchDebounce(searchText, SEARCH_DEBOUNCE_DELAY)
    }

    fun searchInstant(searchText: String) {
        searchDebounce(searchText, 0)
    }

    private fun searchDebounce(searchText: String, delay: Long) {
        lastSearchText = searchText
        handler.removeCallbacksAndMessages(SEARCH_REQUEST_TOKEN)

        val searchRequest = Runnable { search(searchText) }
        handler.postAtTime(searchRequest, SEARCH_REQUEST_TOKEN, SystemClock.uptimeMillis() + delay)
    }

    private fun search(searchText: String) {
        renderState(SearchState.Loading)

        trackInteractor.search(searchText, object : TracksInteractor.TracksConsumer {
            override fun consume(foundTracks: List<Track>?) {
                val tracks = mutableListOf<Track>()
                if (foundTracks != null) {
                    tracks.addAll(foundTracks)
                    renderState(SearchState.Content(tracks))
                } else {
                    renderState(SearchState.Error)
                }
            }
        })
    }

    fun saveToHistory(track: Track) {
        trackInteractor.saveToHistory(track)
    }

    fun clearHistory() {
        trackInteractor.clearHistory()
        renderState(SearchState.History(emptyList()))
    }

    fun getHistory() {
        lastSearchText = ""
        handler.removeCallbacksAndMessages(SEARCH_REQUEST_TOKEN)

        trackInteractor.getHistory(object : TracksInteractor.TracksConsumer {
            override fun consume(foundTracks: List<Track>?) {
                val tracks = mutableListOf<Track>()
                foundTracks?.let { tracks.addAll(it) }
                renderState(SearchState.History(tracks))
            }
        })
    }

    private fun renderState(state: SearchState) {
        stateLiveData.postValue(state)
    }
}