package com.practicum.playlistmaker.search.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import com.practicum.playlistmaker.creator.Creator
import com.practicum.playlistmaker.search.data.StorageClient
import java.lang.reflect.Type

class TrackStorageClient<T>(
    private val context: Context,
    private val dataKey: String,
    private val type: Type
) : StorageClient<T> {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(TRACK_STORAGE, MODE_PRIVATE)
    private val gson = Creator.createGson()


    override fun storeData(data: T) {
        val json = gson.toJson(data, type)
        sharedPreferences.edit().putString(dataKey, json).apply()
    }

    override fun getData(): T? {
        val json = sharedPreferences.getString(dataKey, "[]")
        return gson.fromJson(json, type)
    }

    override fun clearData() {
        sharedPreferences.edit().remove(dataKey).apply()
    }

    companion object {
        private const val TRACK_STORAGE = "track_storage"
    }
}