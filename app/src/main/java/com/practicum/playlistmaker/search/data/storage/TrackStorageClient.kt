package com.practicum.playlistmaker.search.data.storage

import android.content.SharedPreferences
import com.google.gson.Gson
import com.practicum.playlistmaker.search.data.StorageClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Type

class TrackStorageClient<T>(
    private val dataKey: String,
    private val type: Type,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : StorageClient<T> {

    override suspend fun storeData(data: T) {
        withContext(Dispatchers.IO) {
            val json = gson.toJson(data, type)
            sharedPreferences.edit().putString(dataKey, json).apply()
        }
    }

    override suspend fun getData(): T? {
        return withContext(Dispatchers.IO) {
            val json = sharedPreferences.getString(dataKey, "[]")
            gson.fromJson(json, type)
        }
    }

    override suspend fun clearData() {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().remove(dataKey).apply()
        }
    }
}