package com.practicum.playlistmaker.library.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.PlaylistsState
import com.practicum.playlistmaker.library.domain.api.PlaylistsInteractor
import com.practicum.playlistmaker.library.domain.model.Playlist
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private val stateLiveData = MutableLiveData<PlaylistsState>()
    fun observeState(): LiveData<PlaylistsState> = stateLiveData

    fun uploadPlaylist() {
        renderState(PlaylistsState.Loading)
        viewModelScope.launch {
            playlistsInteractor.getAllPlaylists()
                .collect { foundPlaylists ->
                    if (foundPlaylists.isEmpty()) {
                        renderState(PlaylistsState.Empty)
                    } else {
                        val playlists = mutableListOf<Playlist>()
                        playlists.addAll(foundPlaylists)
                        renderState(PlaylistsState.Content(playlists))
                    }
                }
        }
    }

    private fun renderState(state: PlaylistsState) {
        stateLiveData.postValue(state)
    }
}