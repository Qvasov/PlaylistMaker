package com.practicum.playlistmaker.utils

fun getTrackWord(number: Int): String {
    val n = Math.abs(number) % 100
    val n1 = n % 10

    return when {
        n in 11..19 -> "$number треков"
        n1 == 1 -> "$number трек"
        n1 in 2..4 -> "$number трека"
        else -> "$number треков"
    }
}

fun getMinutesWord(number: Int): String {
    val n = Math.abs(number) % 100
    val n1 = n % 10

    return when {
        n in 11..14 -> "$number минут"
        n1 == 1 -> "$number минута"
        n1 in 2..4 -> "$number минуты"
        else -> "$number минут"
    }
}

