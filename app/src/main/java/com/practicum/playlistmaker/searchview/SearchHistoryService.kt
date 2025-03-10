package com.practicum.playlistmaker.searchview

import android.content.SharedPreferences
import com.google.gson.Gson
import com.practicum.playlistmaker.api.Track

class SearchHistoryService(val sharedPreferences: SharedPreferences) {
    companion object {
        private const val TRACK_HISTORY = "track_history"
    }

    private val gson = Gson()

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