package com.practicum.playlistmaker.player.ui

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

class PlaylistSheetAdapter(
    private val playlistSheetClickListener: PlaylistClickListener
) : RecyclerView.Adapter<PlaylistSheetAdapter.PlaylistSheetHolder>() {

    var playlists: List<Playlist> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistSheetHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_sheet_view, parent, false)
        return PlaylistSheetHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistSheetHolder, position: Int) {
        holder.bind(playlists[position])
        holder.itemView.setOnClickListener {
            playlistSheetClickListener.onPlaylistClick(playlists[position])
        }
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    class PlaylistSheetHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val coverViewImage = itemView.findViewById<ImageView>(R.id.sheetPlaylistCover)
        private val titleViewText = itemView.findViewById<TextView>(R.id.sheetPlaylistTitle)
        private val trackCountViewText = itemView.findViewById<TextView>(R.id.sheetPlaylistTracksCount)

        fun bind(playlist: Playlist) {
            Glide.with(itemView)
                .load(playlist.coverUri)
                .placeholder(R.drawable.album_place_holder)
                .transform(CenterCrop(), RoundedCorners((itemView.context.resources.displayMetrics.density * 2 + 0.5f).toInt()))
                .into(coverViewImage)
            titleViewText.text = playlist.name
            trackCountViewText.text = getTrackWord(playlist.tracksCount)
        }

        private fun getTrackWord(number: Int): String {
            val n = Math.abs(number) % 100
            val n1 = n % 10

            return when {
                n in 11..19 -> "$number треков"
                n1 == 1 -> "$number трек"
                n1 in 2..4 -> "$number трека"
                else -> "$number треков"
            }
        }
    }

    fun interface PlaylistClickListener {
        fun onPlaylistClick(playlist: Playlist)
    }
}