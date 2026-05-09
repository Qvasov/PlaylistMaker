package com.practicum.playlistmaker.search.data

import com.practicum.playlistmaker.search.data.dto.iTunesApiRequest
import com.practicum.playlistmaker.search.data.dto.iTunesApiResponse
import com.practicum.playlistmaker.search.domain.api.TracksRepository
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class TracksRepositoryImpl(
    private val networkClient: NetworkClient,
    private val storageClient: StorageClient<ArrayList<Track>>
) : TracksRepository {
    override fun search(expression: String): Flow<List<Track>?> = flow {
        val response = networkClient.doRequest(iTunesApiRequest(expression))
        if (response.resultCode == 200) {
            val data = (response as iTunesApiResponse).results.map {
                Track(
                    it.trackId,
                    it.trackName,
                    it.artistName,
                    it.getSimpleTrackTime(),
                    it.getCoverArtwork(),
                    it.collectionName,
                    it.getReleaseYear(),
                    it.primaryGenreName,
                    it.country,
                    it.previewUrl
                )
            }
            emit(data)
        } else if (response.resultCode == 404) {
            emit(emptyList())
        } else {
            emit(null)
        }
    }

    override fun saveToHistory(track: Track) {
        GlobalScope.launch {
            val historyTrackList = storageClient.getData() ?: ArrayList()
            if (historyTrackList.remove(track) || historyTrackList.size < 10) {
                historyTrackList.add(track)
            } else {
                historyTrackList.removeAt(0)
                historyTrackList.add(track)
            }

            storageClient.storeData(historyTrackList) }
    }

    override fun getHistory(): Flow<List<Track>?> = flow {
        emit(storageClient.getData()?.reversed()?.toMutableList())
    }

    override fun clearHistory() {
        GlobalScope.launch { storageClient.clearData() }
    }
}