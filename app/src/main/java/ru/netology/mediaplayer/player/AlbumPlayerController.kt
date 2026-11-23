package ru.netology.mediaplayer.player

import android.media.MediaPlayer

data class PlayerUiState(
    val currentIndex: Int? = null,
    val isPlaying: Boolean = false,
    val isPreparing: Boolean = false
)

class AlbumPlayerController(
    private val baseUrl: String,
    private val onStateChanged: (PlayerUiState) -> Unit
) {
    private var tracks: List<String> = emptyList()
    private var currentIndex: Int? = null
    private var isPlaying: Boolean = false
    private var isPrepared: Boolean = false
    private var isPreparing: Boolean = false
    private var shouldPlayWhenPrepared = false
    private val mediaPlayer = MediaPlayer().apply {
        setOnPreparedListener { mp ->
            isPreparing = false
            isPrepared = true
            if (shouldPlayWhenPrepared) {
                mp.start()
                this@AlbumPlayerController.isPlaying = true
            } else {
                this@AlbumPlayerController.isPlaying = false
            }
            notifyState()
        }
        setOnCompletionListener {
            playNext()
        }
        setOnErrorListener { _, _, _ ->
            isPreparing = false
            this@AlbumPlayerController.isPlaying = false
            isPrepared = false
            shouldPlayWhenPrepared = false
            notifyState()
            true
        }
    }

    private fun notifyState() {
        onStateChanged(
            PlayerUiState(
                currentIndex = currentIndex,
                isPlaying = isPlaying,
                isPreparing = isPreparing
            )
        )
    }

    private fun startLoadingTrack(index: Int) {
        val fileName = tracks.getOrNull(index) ?: return
        val url = baseUrl + fileName
        currentIndex = index
        isPrepared = false
        isPreparing = true
        isPlaying = false
        shouldPlayWhenPrepared = true
        notifyState()
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            isPreparing = false
            isPrepared = false
            isPlaying = false
            shouldPlayWhenPrepared = false
            notifyState()
        }
    }

    fun playNext() {
        if (tracks.isEmpty()) {
            return
        }
        val current = currentIndex ?: -1
        val nextIndex = (current + 1) % tracks.size
        startLoadingTrack(nextIndex)
    }

    fun submitTracks(fileNames: List<String>) {
        tracks = fileNames
        currentIndex = null
        isPlaying = false
        isPrepared = false
        isPreparing = false
        shouldPlayWhenPrepared = false
        try {
            mediaPlayer.reset()
        } catch (_: Exception) {
        }
        notifyState()
    }

    fun playTrackAt(index: Int) {
        if (index !in tracks.indices) {
            return
        }
        if (currentIndex == index) {
            togglePlayPause()
            return
        }
        startLoadingTrack(index)
    }

    fun togglePlayPause() {
        if (tracks.isEmpty()) {
            return
        }
        val index = currentIndex
        if (index == null) {
            startLoadingTrack(0)
            return
        }
        if (isPlaying) {
            shouldPlayWhenPrepared = false
            if (isPrepared) {
                mediaPlayer.pause()
            }
            isPlaying = false
            notifyState()
        } else if (!isPreparing) {
            shouldPlayWhenPrepared = true
            if (isPrepared) {
                mediaPlayer.start()
                isPlaying = true
                notifyState()
            } else {
                startLoadingTrack(index)
            }
        }
    }

    fun release() {
        isPlaying = false
        isPrepared = false
        isPreparing = false
        shouldPlayWhenPrepared = false
        mediaPlayer.release()
    }
}