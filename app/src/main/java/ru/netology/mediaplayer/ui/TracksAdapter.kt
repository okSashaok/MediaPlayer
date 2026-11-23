package ru.netology.mediaplayer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.mediaplayer.R

data class TrackRowUi(
    val id: Long,
    val title: String?,
    val durationText: String,
    val isCurrent: Boolean,
    val isPlaying: Boolean
)

class TrackViewHolder(
    itemView: View,
    private val onTrackClick: (position: Int) -> Unit
) : RecyclerView.ViewHolder(itemView) {
    private val playButton = itemView.findViewById<ImageButton>(R.id.trackPlayButton)
    private val titleView = itemView.findViewById<TextView>(R.id.trackTitle)
    private val durationView = itemView.findViewById<TextView>(R.id.trackDuration)

    init {
        val clickListener = View.OnClickListener {
            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onTrackClick(pos)
            }
        }
        itemView.setOnClickListener(clickListener)
        playButton.setOnClickListener(clickListener)
    }

    fun bindPlaybackState(item: TrackRowUi) {
        val showPause = item.isCurrent && item.isPlaying
        val iconRes = if (showPause) {
            R.drawable.pause_circle_24dp
        } else {
            R.drawable.play_circle_24dp
        }
        playButton.setImageResource(iconRes)
        playButton.contentDescription = if (showPause) {
            "Pause"
        } else {
            "Play track"
        }
        titleView.isSelected = item.isCurrent
    }

    fun bind(item: TrackRowUi) {
        titleView.text = item.title
        durationView.text = item.durationText
        bindPlaybackState(item)
    }
}

private object DiffCallback : DiffUtil.ItemCallback<TrackRowUi>() {
    private const val PAYLOAD_PLAYBACK = "playback_state"
    override fun areItemsTheSame(oldItem: TrackRowUi, newItem: TrackRowUi): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TrackRowUi, newItem: TrackRowUi): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: TrackRowUi, newItem: TrackRowUi): Any? {
        return if (
            oldItem.title == newItem.title
            && oldItem.durationText == newItem.durationText
            && (
                    oldItem.isCurrent != newItem.isCurrent
                            || oldItem.isPlaying != newItem.isPlaying
                    )
        ) {
            PAYLOAD_PLAYBACK
        } else {
            null
        }
    }
}

class TracksAdapter(
    private val onTrackClick: (position: Int) -> Unit
) : androidx.recyclerview.widget.ListAdapter<TrackRowUi, TrackViewHolder>(DiffCallback) {
    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: TrackViewHolder,
        position: Int,
        payload: MutableList<Any>
    ) {
        if (payload.isNotEmpty()) {
            holder.bindPlaybackState(getItem(position))
        } else {
            super.onBindViewHolder(holder, position, payload)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view, onTrackClick)
    }
}