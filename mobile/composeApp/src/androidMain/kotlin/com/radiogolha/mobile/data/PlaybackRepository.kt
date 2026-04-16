package com.radiogolha.mobile.data

import android.content.Context
import com.radiogolha.mobile.RustCoreBridge
import org.json.JSONArray
import java.io.File

class PlaybackRepository(context: Context) {
    private val userDbPath: String = File(context.filesDir, "user_data.db").absolutePath

    fun recordPlayback(trackId: Long) {
        RustCoreBridge.recordPlayback(userDbPath, trackId)
    }

    fun getRecentlyPlayedIds(limit: Long = 20): List<Long> {
        val result = RustCoreBridge.getRecentlyPlayedIds(userDbPath, limit)
        return parseIds(result)
    }

    fun getMostPlayedIds(limit: Long = 20): List<Long> {
        val result = RustCoreBridge.getMostPlayedIds(userDbPath, limit)
        return parseIds(result)
    }

    private fun parseIds(json: String): List<Long> {
        val arr = try { JSONArray(json) } catch (e: Exception) { JSONArray() }
        return buildList {
            for (i in 0 until arr.length()) {
                add(arr.getLong(i))
            }
        }
    }
}
