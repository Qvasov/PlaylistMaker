package com.practicum.playlistmaker.library.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.FavoritesState
import com.practicum.playlistmaker.library.domain.api.FavoritesInteractor
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoritesInteractor: FavoritesInteractor
) : ViewModel() {

    private val stateLiveData = MutableLiveData<FavoritesState>()
    fun observeState(): LiveData<FavoritesState> = stateLiveData

    fun uploadFavorites() {
        renderState(FavoritesState.Loading)
        viewModelScope.launch {
            favoritesInteractor.getAllTracks()
                .collect { favoriteTracks ->
                    if (favoriteTracks.isEmpty()) {
                        renderState(FavoritesState.Empty)
                    } else {
                        val tracks = mutableListOf<Track>()
                        tracks.addAll(favoriteTracks)
                        renderState(FavoritesState.Content(tracks))
                    }
                }
        }
    }

    private fun renderState(state: FavoritesState) {
        stateLiveData.postValue(state)
    }
}