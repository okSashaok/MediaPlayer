package ru.netology.mediaplayer.model

data class TrackItem(
    val id: Long,
    val file: String,
    val title: String? = null,
    val artist: String? = null,
    val duration: Long = 0
)