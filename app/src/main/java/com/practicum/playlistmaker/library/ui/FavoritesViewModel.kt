package com.practicum.playlistmaker.library.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.LibraryState
import com.practicum.playlistmaker.library.domain.api.FavoritesInteractor
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoritesInteractor: FavoritesInteractor
) : ViewModel() {

    private val stateLiveData = MutableLiveData<LibraryState>()
    fun observeState(): LiveData<LibraryState> = stateLiveData

    fun uploadFavorites() {
        renderState(LibraryState.Loading)
        viewModelScope.launch {
            favoritesInteractor.getAllTracks()
                .collect { favoriteTracks ->
                    if (favoriteTracks.isEmpty()) {
                        renderState(LibraryState.Empty)
                    } else {
                        val tracks = mutableListOf<Track>()
                        tracks.addAll(favoriteTracks)
                        renderState(LibraryState.Content(tracks))
                    }
                }
        }
    }

    private fun renderState(state: LibraryState) {
        stateLiveData.postValue(state)
    }
}