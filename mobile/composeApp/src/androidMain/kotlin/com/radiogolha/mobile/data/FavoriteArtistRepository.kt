package com.radiogolha.mobile.data

import android.content.Context
import com.radiogolha.mobile.RustCoreBridge
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class FavoriteArtistRepository(context: Context) {
    private val userDbPath: String = File(context.filesDir, "user_data.db").absolutePath

    fun addFavorite(artistId: Long, type: String = "singer") {
        RustCoreBridge.addFavoriteArtist(userDbPath, artistId, type)
    }

    fun removeFavorite(artistId: Long) {
        RustCoreBridge.removeFavoriteArtist(userDbPath, artistId)
    }

    fun isFavorite(artistId: Long): Boolean {
        val result = RustCoreBridge.isFavoriteArtist(userDbPath, artistId)
        return runCatching { JSONObject(result).optBoolean("favorite", false) }.getOrDefault(false)
    }

    fun toggleFavorite(artistId: Long, type: String = "singer"): Boolean {
        return if (isFavorite(artistId)) {
            removeFavorite(artistId)
            false
        } else {
            addFavorite(artistId, type)
            true
        }
    }

    fun getFavoriteIds(type: String = ""): List<Long> {
        val result = RustCoreBridge.getFavoriteArtistIds(userDbPath, type)
        val arr = runCatching { JSONArray(result) }.getOrDefault(JSONArray())
        return buildList { for (i in 0 until arr.length()) add(arr.getLong(i)) }
    }
}
