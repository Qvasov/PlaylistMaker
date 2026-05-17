package com.practicum.playlistmaker.library.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.library.domain.model.Playlist
import com.practicum.playlistmaker.utils.getTrackWord

class PlaylistsAdapter(
    private val playlistClickListener: PlaylistClickListener
) : RecyclerView.Adapter<PlaylistsAdapter.PlaylistHolder>() {

    var playlists: List<Playlist> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_view, parent, false)
        return PlaylistHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistHolder, position: Int) {
        holder.bind(playlists[position])
        holder.itemView.setOnClickListener {
            playlistClickListener.onPlaylistClick(playlists[position])
        }
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    class PlaylistHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val coverViewImage = itemView.findViewById<ImageView>(R.id.playlistCover)
        private val titleViewText = itemView.findViewById<TextView>(R.id.playlistTitle)
        private val trackCountViewText = itemView.findViewById<TextView>(R.id.playlistTracksCount)

        fun bind(playlist: Playlist) {
            Glide.with(itemView)
                .load(playlist.coverUri)
                .placeholder(R.drawable.playlist_view_placeholder)
                .transform(CenterCrop(), RoundedCorners((itemView.context.resources.displayMetrics.density * 8 + 0.5f).toInt()))
                .into(coverViewImage)
            titleViewText.text = playlist.name
            trackCountViewText.text = getTrackWord(playlist.tracksCount)
        }
    }

    fun interface PlaylistClickListener {
        fun onPlaylistClick(playlist: Playlist)
    }
}