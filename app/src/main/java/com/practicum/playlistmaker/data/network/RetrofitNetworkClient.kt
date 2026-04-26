package com.practicum.playlistmaker.data.network

import com.practicum.playlistmaker.data.NetworkClient
import com.practicum.playlistmaker.data.dto.Response
import com.practicum.playlistmaker.data.dto.iTunesApiRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitNetworkClient : NetworkClient {
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

    private val iTunesService = createITunesController();

    override fun doRequest(dto: Any): Response {
        if (dto is iTunesApiRequest) {
            val resp = iTunesService.search(dto.expression).execute()

            val body = resp.body() ?: Response()

            return body.apply { resultCode = resp.code() }
        } else {
            return Response().apply { resultCode = 400 }
        }
    }
}