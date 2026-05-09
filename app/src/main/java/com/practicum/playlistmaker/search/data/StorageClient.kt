package com.practicum.playlistmaker.search.data

interface StorageClient<T> {
    suspend fun storeData(data: T)
    suspend fun getData(): T?
    suspend fun clearData()
}