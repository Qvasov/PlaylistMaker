package com.practicum.playlistmaker.searchview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.api.Track
import java.text.SimpleDateFormat
import java.util.Locale

class TrackAdapter(
    private val trackList: List<Track>
) : RecyclerView.Adapter<TrackAdapter.TrackHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_view, parent, false)
        return TrackHolder(view)
    }

    override fun onBindViewHolder(holder: TrackHolder, position: Int) {
        holder.bind(trackList[position])
    }

    override fun getItemCount(): Int {
        return trackList.size
    }

    class TrackHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackViewImage = itemView.findViewById<ImageView>(R.id.trackViewImage)
        private val trackViewTextTrackName = itemView.findViewById<TextView>(R.id.trackViewTextTrackName)
        private val trackViewTextArtisName = itemView.findViewById<TextView>(R.id.trackViewTextArtisName)
        private val trackViewTextTrackTime = itemView.findViewById<TextView>(R.id.trackViewTextTrackTime)

        fun bind(track: Track) {
            Glide.with(itemView)
                .load(track.artworkUrl100)
                .placeholder(R.drawable.album_place_holder)
                .centerInside()
                .transform(RoundedCorners(2))
                .into(trackViewImage)
            trackViewTextTrackName.text = track.trackName
            trackViewTextArtisName.text = track.artistName
            trackViewTextTrackTime.text = SimpleDateFormat("mm:ss", Locale.getDefault())
                .format(track.trackTime).toString()
        }
    }
}