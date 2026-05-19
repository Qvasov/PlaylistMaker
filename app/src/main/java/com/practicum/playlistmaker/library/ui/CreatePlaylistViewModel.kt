package com.practicum.playlistmaker.library.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.api.PlaylistsInteractor
import com.practicum.playlistmaker.library.domain.model.Playlist
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

open class CreatePlaylistViewModel(
    private val context: Context,
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    fun createPlaylist(playlistName: String, playListDescription: String?, imageUri: Uri?) {
        viewModelScope.launch {
            var iternalImageUri: Uri? = null
            if (imageUri != null) {
                iternalImageUri = saveImageToPrivateStorage(playlistName, imageUri)
            }
            playlistsInteractor.addPlaylist(
                Playlist(
                    name = playlistName,
                    description = playListDescription,
                    coverUri = iternalImageUri,
                    trackIds = emptyList(),
                    tracksCount = 0
                )
            )
        }

    }

    protected fun saveImageToPrivateStorage(playlistName: String, imageUri: Uri): Uri {
        val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), ALBUM_NAME)
        if (!filePath.exists()) {
            filePath.mkdirs()
        }
        val file = File(filePath, "$playlistName$_COVER.jpg")
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val outputStream = FileOutputStream(file)
        BitmapFactory
            .decodeStream(inputStream)
            .compress(Bitmap.CompressFormat.JPEG, 30, outputStream)
        return file.toUri()
    }

    protected fun deleteImageToPrivateStorage(playlistName: String) {
        val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), ALBUM_NAME)
        if (!filePath.exists()) {
            return
        }
        val file = File(filePath, "$playlistName$_COVER.jpg")
        if (file.exists()) {
            file.delete()
        }
    }

    companion object {
        private const val ALBUM_NAME = "playlists_covers"
        private const val _COVER = "_cover"
    }

}