package com.practicum.playlistmaker.search.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.search.domain.api.TracksInteractor
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.utils.debounce
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchViewModel(
    private val trackInteractor: TracksInteractor
) : ViewModel() {

    private var lastSearchText: String? = null
    private val trackSearchDebounce = debounce<String>(viewModelScope, true) {
        searchText -> search(searchText)
    }
    private var trackSearchJob: Job? = null

    private val stateLiveData = MutableLiveData<SearchState>()
    fun observeState(): LiveData<SearchState> = stateLiveData

    override fun onCleared() {
        super.onCleared()
        trackSearchJob?.cancel()
    }

    fun searchDebounce(searchText: String) {
        if (lastSearchText != searchText) {
            lastSearchText = searchText
            trackSearchJob = trackSearchDebounce(searchText, SEARCH_DEBOUNCE_DELAY)
        }
    }

    fun searchInstant(searchText: String) {
        trackSearchJob = trackSearchDebounce(searchText, 0)
    }

    private fun search(searchText: String) {
        renderState(SearchState.Loading)

        viewModelScope.launch {
            trackInteractor.search(searchText)
                .collect { foundTracks ->
                    if (foundTracks != null) {
                        val tracks = mutableListOf<Track>()
                        tracks.addAll(foundTracks)
                        renderState(SearchState.Content(tracks))
                    } else {
                        renderState(SearchState.Error)
                    }
                }
        }
    }

    fun saveToHistory(track: Track) {
        viewModelScope.launch {
            trackInteractor.saveToHistory(track)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            trackInteractor.clearHistory()
        }
        renderState(SearchState.History(emptyList()))
    }

    fun uploadHistory() {
        lastSearchText = ""
        trackSearchJob?.cancel()
        viewModelScope.launch {
            trackInteractor.getHistory()
                .collect {foundTracks ->
                    val tracks = mutableListOf<Track>()
                    foundTracks?.let { tracks.addAll(it) }
                    renderState(SearchState.History(tracks))
                }
        }
    }

    private fun renderState(state: SearchState) {
        stateLiveData.postValue(state)
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}