package ru.netology.mediaplayer.data

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import ru.netology.mediaplayer.model.AlbumModel

object AlbumLoader {
    private const val ALBUM_URL =
        "https://github.com/netology-code/andad-homeworks/raw/master/09_multimedia/data/album.json"
    private val client = OkHttpClient()
    private val gson = Gson()
    suspend fun loadAlbum(): AlbumModel = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(ALBUM_URL)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Error loading album: ${response.code}")
            }
            val body = response.body.string()
            gson.fromJson(body, AlbumModel::class.java)
        }
    }
}