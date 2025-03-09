package com.practicum.playlistmaker

import com.practicum.playlistmaker.api.iTunesApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitService {
    companion object {
        private val ITUNES_BASE_URL = "https://itunes.apple.com"

        fun createITunesController() : iTunesApi  {
            return buildRetrofit(ITUNES_BASE_URL).create(iTunesApi::class.java)
        }

        private fun buildRetrofit(baseUrl: String): Retrofit {
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}