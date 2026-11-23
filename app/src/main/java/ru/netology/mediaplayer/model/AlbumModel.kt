package ru.netology.mediaplayer.model

data class AlbumModel(
    val id: Long,
    val title: String,
    val subtitle: String? = null,
    val artist: String,
    val published: String? = null,
    val genre: String? = null,
    val tracks: List<TrackItem> = emptyList()
)