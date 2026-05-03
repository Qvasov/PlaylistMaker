package com.practicum.playlistmaker.search.data

import com.practicum.playlistmaker.search.data.dto.iTunesApiRequest
import com.practicum.playlistmaker.search.data.dto.iTunesApiResponse
import com.practicum.playlistmaker.search.domain.api.TracksRepository
import com.practicum.playlistmaker.search.domain.models.Track

class TracksRepositoryImpl(
    private val networkClient: NetworkClient,
    private val storageClient: StorageClient<ArrayList<Track>>
) : TracksRepository {
    override fun search(expression: String): List<Track>? {
        val response = networkClient.doRequest(iTunesApiRequest(expression))
        if (response.resultCode == 200) {
            return (response as iTunesApiResponse).results.map {
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
        } else if (response.resultCode == 404) {
            return emptyList()
        } else {
            return null
        }
    }

    override fun saveToHistory(track: Track) {
        val historyTrackList = storageClient.getData() ?: ArrayList()

        if (historyTrackList.remove(track) || historyTrackList.size < 10) {
            historyTrackList.add(track)
        } else {
            historyTrackList.removeAt(0)
            historyTrackList.add(track)
        }

        storageClient.storeData(historyTrackList)
    }

    override fun getHistory(): List<Track>? {
        return storageClient.getData()?.reversed()?.toMutableList()
    }

    override fun clearHistory() {
        storageClient.clearData()
    }
}