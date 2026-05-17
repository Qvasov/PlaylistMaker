package com.practicum.playlistmaker.playlist.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.api.PlaylistsInteractor
import com.practicum.playlistmaker.library.domain.model.Playlist
import com.practicum.playlistmaker.playlist.domain.PlaylistState
import com.practicum.playlistmaker.playlist.domain.TrackListSheetState
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private val stateLiveData = MutableLiveData<PlaylistState>()
    fun observeState(): LiveData<PlaylistState> = stateLiveData

    private val bottomSheetStateLiveData = MutableLiveData<TrackListSheetState>()
    fun observeBottomSheetState(): LiveData<TrackListSheetState> = bottomSheetStateLiveData

    fun uploadPlaylist(id: Long) {
        viewModelScope.launch {
            playlistsInteractor.getPlaylistById(id).collect {
                stateLiveData.postValue(PlaylistState.Content(it))
            }
        }
    }

    fun uploadTracks(playlistId: Long) {
        renderSheetState(TrackListSheetState.Loading)
        viewModelScope.launch {
            playlistsInteractor.getTrackListById(playlistId)
                .collect { foundTracks ->
                    if (foundTracks.isEmpty()) {
                        renderSheetState(TrackListSheetState.Empty)
                    } else {
                        val trackList = mutableListOf<Track>()
                        trackList.addAll(foundTracks)
                        renderSheetState(TrackListSheetState.Content(trackList))
                    }
                }
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistsInteractor.deletePlaylist(playlist)
        }
    }

    fun deleteTrackFromPlaylist(track: Track, playlist: Playlist) {
        viewModelScope.launch {
            playlistsInteractor.deleteFromPlaylist(track, playlist)
            uploadTracks(playlist.id!!)
        }
    }

    private fun renderSheetState(state: TrackListSheetState) {
        bottomSheetStateLiveData.postValue(state)
    }
}