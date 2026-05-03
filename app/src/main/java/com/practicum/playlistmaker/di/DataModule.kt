package com.practicum.playlistmaker.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.search.data.NetworkClient
import com.practicum.playlistmaker.search.data.StorageClient
import com.practicum.playlistmaker.search.data.network.RetrofitNetworkClient
import com.practicum.playlistmaker.search.data.network.iTunesApi
import com.practicum.playlistmaker.search.data.storage.TrackStorageClient
import com.practicum.playlistmaker.search.domain.models.Track
import org.koin.android.ext.koin.androidContext
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val ITUNES_BASE_URL = "https://itunes.apple.com"
private const val TRACK_STORAGE = "track_storage"


val dataModule = module {
    single<iTunesApi> {
        Retrofit.Builder()
            .baseUrl(ITUNES_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(iTunesApi::class.java)
    }

    single<SharedPreferences> { params ->
        androidContext()
            .getSharedPreferences(params.get(), Context.MODE_PRIVATE)
    }

    factory<Gson> { Gson() }

    single<NetworkClient> {
        RetrofitNetworkClient(androidContext(), get())
    }

    single<StorageClient<ArrayList<Track>>> { params ->
        TrackStorageClient(
            params.get(),
            object : TypeToken<ArrayList<Track>>() {}.type,
            get { parametersOf(TRACK_STORAGE) },
            get()
        )
    }
}