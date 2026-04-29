package com.practicum.playlistmaker.data

import com.practicum.playlistmaker.data.dto.iTunesApiRequest
import com.practicum.playlistmaker.data.dto.iTunesApiResponse
import com.practicum.playlistmaker.data.searchview.SearchHistoryService
import com.practicum.playlistmaker.domain.api.TracksRepository
import com.practicum.playlistmaker.domain.models.Track

class TracksRepositoryImpl(private val networkClient: NetworkClient,
                           private val searchHistoryService: SearchHistoryService) : TracksRepository {
    override fun search(expression: String): List<Track>? {
        val response = networkClient.doRequest(iTunesApiRequest(expression))
        if (response.resultCode == 200) {
            return (response as iTunesApiResponse).results.map {
                Track(it.trackId,
                    it.trackName,
                    it.artistName,
                    it.getSimpleTrackTime(),
                    it.getCoverArtwork(),
                    it.collectionName,
                    it.getReleaseYear(),
                    it.primaryGenreName,
                    it.country,
                    it.previewUrl) }
        } else if (response.resultCode == 404) {
            return emptyList()
        } else {
            return null
        }
    }

    override fun saveToHistory(track: Track) {
        searchHistoryService.addTrackToTrackHistory(track)
    }

    override fun getHistory(): List<Track> {
        return searchHistoryService.getTrackHistory()
    }

    override fun clearHistory() {
        searchHistoryService.clearTrackHistory()
    }
}