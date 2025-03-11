package com.practicum.playlistmaker

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.practicum.playlistmaker.api.Track

class PlayerActivity : AppCompatActivity() {
    companion object{
        const val TRACK = "TRACK"
        private const val START_TIME = "0:00"
    }

    private val gson = Gson()
    private lateinit var track: Track

    private lateinit var backButton: ImageView
    private lateinit var coverArtwork: ImageView
    private lateinit var trackName: TextView
    private lateinit var artistName: TextView
    private lateinit var playTime: TextView
    private lateinit var trackTime: TextView
    private lateinit var collectionName: TextView
    private lateinit var releaseDate: TextView
    private lateinit var primaryGenreName: TextView
    private lateinit var country: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)

        track = gson.fromJson(intent.getStringExtra(TRACK), Track::class.java)

        backButton = findViewById(R.id.backButton)
        coverArtwork = findViewById(R.id.coverArtwork)
        trackName = findViewById(R.id.trackName)
        playTime = findViewById(R.id.playTime)
        artistName = findViewById(R.id.artistName)
        trackTime = findViewById(R.id.trackTime)
        collectionName = findViewById(R.id.collectionName)
        releaseDate = findViewById(R.id.releaseDate)
        primaryGenreName = findViewById(R.id.primaryGenreName)
        country = findViewById(R.id.country)

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.album_place_holder)
            .fitCenter()
            .transform(RoundedCorners(8))
            .into(coverArtwork)
        trackName.text = track.trackName
        artistName.text = track.artistName
        playTime.text = START_TIME
        trackTime.text = track.getSimpleTrackTime()
        collectionName.text = track.collectionName
        releaseDate.text = track.getReleaseYear()
        primaryGenreName.text = track.primaryGenreName
        country.text = track.country

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.player)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}