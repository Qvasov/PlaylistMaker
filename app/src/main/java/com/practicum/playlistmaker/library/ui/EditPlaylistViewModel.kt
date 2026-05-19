package com.practicum.playlistmaker.library.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.api.PlaylistsInteractor
import com.practicum.playlistmaker.library.domain.model.Playlist
import com.practicum.playlistmaker.playlist.domain.PlaylistState
import kotlinx.coroutines.launch

class EditPlaylistViewModel(
    private val context: Context,
    private val playlistsInteractor: PlaylistsInteractor
) : CreatePlaylistViewModel(context, playlistsInteractor) {

    private val stateLiveData = MutableLiveData<PlaylistState>()
    fun observeState(): LiveData<PlaylistState> = stateLiveData


    fun uploadInfo(playlistId: Long) {
        viewModelScope.launch {
            playlistsInteractor.getPlaylistById(playlistId).collect {
                stateLiveData.postValue(PlaylistState.Content(it))
            }
        }
    }


    fun updatePlaylist(
        playlist: Playlist?,
        playlistName: String,
        playListDescription: String?,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            var iternalImageUri: Uri? = playlist?.coverUri
            if (imageUri != null) {
                deleteImageToPrivateStorage(playlist?.name ?: "")
                iternalImageUri = saveImageToPrivateStorage(playlistName, imageUri)
            }
            playlistsInteractor.addPlaylist(
                Playlist(
                    id = playlist?.id,
                    name = playlistName,
                    description = playListDescription,
                    coverUri = iternalImageUri,
                    trackIds = playlist?.trackIds ?: emptyList(),
                    tracksCount = playlist?.tracksCount ?: 0
                )
            )
        }

    }
}