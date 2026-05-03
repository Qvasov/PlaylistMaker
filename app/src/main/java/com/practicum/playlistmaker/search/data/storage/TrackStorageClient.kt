package com.practicum.playlistmaker.search.data.storage

import android.content.SharedPreferences
import com.google.gson.Gson
import com.practicum.playlistmaker.search.data.StorageClient
import java.lang.reflect.Type

class TrackStorageClient<T>(
    private val dataKey: String,
    private val type: Type,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : StorageClient<T> {

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
}