package ru.netology.mediaplayer.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import ru.netology.mediaplayer.R
import ru.netology.mediaplayer.data.AlbumLoader
import ru.netology.mediaplayer.model.AlbumModel
import ru.netology.mediaplayer.model.TrackItem
import ru.netology.mediaplayer.player.AlbumPlayerController
import ru.netology.mediaplayer.player.PlayerUiState
import kotlin.collections.mapIndexed

class MainActivity : AppCompatActivity() {

    private lateinit var playButton: ImageButton
    private lateinit var albumTitleView: TextView
    private lateinit var albumSubTitleView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var adapter: TracksAdapter
    private var currentTracks: List<TrackItem> = emptyList()
    private var uiState: PlayerUiState = PlayerUiState()
    private val playerController = AlbumPlayerController(
        baseUrl = "https://github.com/netology-code/andad-homeworks/raw/master/09_multimedia/data/",
        onStateChanged = { state ->
            uiState = state
            updateMainPlayButton()
            submitTrackRows()
        }
    )

    private fun updateMainPlayButton() {
        val iconRes = if (uiState.isPlaying) {
            R.drawable.pause_circle_24dp
        } else {
            R.drawable.play_circle_24dp
        }
        playButton.setImageResource(iconRes)
        playButton.isEnabled = currentTracks.isNotEmpty() && !uiState.isPreparing
        playButton.contentDescription = when {
            uiState.isPreparing -> "Loading track"
            uiState.isPlaying -> "Pause"
            else -> "Play"
        }
    }

    private fun submitTrackRows() {
        val rows = currentTracks.mapIndexed { index, track ->
            val isCurrent = index == uiState.currentIndex
            TrackRowUi(
                id = track.id,
                title = track.title,
                durationText = formatDuration(track.duration),
                isCurrent = isCurrent,
                isPlaying = isCurrent && uiState.isPlaying
            )
        }
        adapter.submitList(rows)
    }

    fun bindAlbum(album: AlbumModel) {
        albumTitleView.text = album.title
        val lines = buildList {
            album.subtitle?.takeIf { it.isNotBlank() }?.let { add(it) }
            album.artist.takeIf { it.isNotBlank() }
                ?.let { add(getString(R.string.executor) + ": $it") }
            album.genre?.takeIf { it.isNotBlank() }
                ?.let { add(getString(R.string.genre) + ": $it") }
            album.published?.takeIf { it.isNotBlank() }
                ?.let { add(getString(R.string.publication_date) + ": $it") }
        }
        albumSubTitleView.text = lines.joinToString("\n")
        currentTracks = album.tracks
        playerController.submitTracks(album.tracks.map { it.file })
        submitTrackRows()
    }

    private fun loadAlbumData() {
        loadingProgressBar.visibility = View.VISIBLE
        playButton.isEnabled = false
        lifecycleScope.launch {
            try {
                val album = AlbumLoader.loadAlbum()
                bindAlbum(album)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                loadingProgressBar.visibility = View.GONE
                updateMainPlayButton()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        albumTitleView = findViewById(R.id.albumTitle)
        albumSubTitleView = findViewById(R.id.albumSubTitle)
        playButton = findViewById(R.id.play)
        recyclerView = findViewById(R.id.tracksRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        adapter = TracksAdapter { position ->
            playerController.playTrackAt(position)
        }
        recyclerView.adapter = adapter
        playButton.setOnClickListener {
            playerController.togglePlayPause()
        }
        updateMainPlayButton()
        loadAlbumData()
    }

    private fun formatDuration(secondsOrMillis: Long): String {
        val totalSeconds = if (secondsOrMillis > 10_000) {
            secondsOrMillis / 1_000
        } else {
            secondsOrMillis
        }
        val mins = totalSeconds / 60
        val secs = totalSeconds % 60
        return "%02d:%02d".format(mins, secs)
    }

    override fun onDestroy() {
        playerController.release()
        super.onDestroy()
    }
}