package com.practicum.playlistmaker.search.data

import com.practicum.playlistmaker.library.data.db.AppDatabase
import com.practicum.playlistmaker.search.data.converters.TrackHistoryConverter
import com.practicum.playlistmaker.search.data.dto.TrackHistoryDto
import com.practicum.playlistmaker.search.data.dto.iTunesApiRequest
import com.practicum.playlistmaker.search.data.dto.iTunesApiResponse
import com.practicum.playlistmaker.search.domain.api.TracksRepository
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TracksRepositoryImpl(
    private val networkClient: NetworkClient,
    private val storageClient: StorageClient<ArrayList<TrackHistoryDto>>,
    private val appDatabase: AppDatabase,
    private val trackHistoryConverter: TrackHistoryConverter
) : TracksRepository {
    override fun search(expression: String): Flow<List<Track>?> = flow {
        val response = networkClient.doRequest(iTunesApiRequest(expression))
        when (response.resultCode) {
            200 -> {
                val favoritesTracks = appDatabase.trackDao().getTrackIds()
                val data = (response as iTunesApiResponse).results.map {
                    Track(
                        it.trackId,
                        it.trackName.trim(),
                        it.artistName.trim(),
                        it.trackTime,
                        it.getCoverArtwork(),
                        it.collectionName.trim(),
                        it.getReleaseYear(),
                        it.primaryGenreName.trim(),
                        it.country.trim(),
                        it.previewUrl,
                        favoritesTracks.contains(it.trackId)
                    )
                }
                emit(data)
            }
            404 -> {
                emit(emptyList())
            }
            else -> {
                emit(null)
            }
        }
    }

    override suspend fun saveToHistory(track: Track) {
        val trackHistoryDto = trackHistoryConverter.mapToTrackHistoryDto(track)
        val historyTrackList = storageClient.getData() ?: ArrayList()
        if (historyTrackList.remove(trackHistoryDto) || historyTrackList.size < 10) {
            historyTrackList.add(trackHistoryDto)
        } else {
            historyTrackList.removeAt(0)
            historyTrackList.add(trackHistoryDto)
        }

        storageClient.storeData(historyTrackList)
    }

    override fun getHistory(): Flow<List<Track>?> = flow {
        val favoritesTracks = appDatabase.trackDao().getTrackIds()
        emit(storageClient.getData()
            ?.map {
                val isFavorite = favoritesTracks.contains(it.trackId)
                val track = trackHistoryConverter.mapToTrack(it, isFavorite)
                track
            }
            ?.reversed()
            ?.toMutableList()
        )
    }

    override suspend fun clearHistory() {
        storageClient.clearData()
    }
}