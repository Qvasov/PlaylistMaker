package com.practicum.playlistmaker.data.searchview

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import com.practicum.playlistmaker.Creator
import com.practicum.playlistmaker.domain.models.Track

class SearchHistoryService(context: Context) {
    companion object {
        private const val TRACK_HISTORY = "track_history"
        private const val SEARCH_ACTIVITY = "search_activity"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        SEARCH_ACTIVITY, MODE_PRIVATE
    )

    private val gson = Creator.createGson()

    fun getTrackHistory(): MutableList<Track> {
        val json = sharedPreferences.getString(TRACK_HISTORY, "[]")
        return gson.fromJson(json, Array<Track>::class.java).reversed().toMutableList()
    }

    fun addTrackToTrackHistory(track: Track) {
        val historyTrackList = getTrackHistory()

        if (historyTrackList.remove(track) || historyTrackList.size < 10) {
            historyTrackList.add(track)
        } else {
            historyTrackList.removeAt(0)
            historyTrackList.add(track)
        }

        val json = gson.toJson(historyTrackList)
        sharedPreferences.edit()
            .putString(TRACK_HISTORY, json)
            .apply()
    }

    fun clearTrackHistory() {
        sharedPreferences.edit().remove(TRACK_HISTORY).apply()
    }
}