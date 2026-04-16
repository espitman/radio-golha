package com.radiogolha.mobile.data

import android.content.Context
import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.search.ActiveFilters
import com.radiogolha.mobile.ui.search.MatchMode
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

enum class PlaylistType { SEARCH, MANUAL }

data class PlaylistEntry(
    val id: Long,
    val name: String,
    val type: PlaylistType = PlaylistType.SEARCH,
    val filtersJson: String = "{}",
    val trackIds: List<Long> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
)

class PlaylistRepository(context: Context) {
    private val userDbPath: String = File(context.filesDir, "user_data.db").absolutePath

    fun getAll(): List<PlaylistEntry> {
        val payload = RustCoreBridge.getAllPlaylists(userDbPath)
        return parsePlaylistArray(payload)
    }

    fun getById(id: Long): PlaylistEntry? {
        val payload = RustCoreBridge.getPlaylist(userDbPath, id)
        val obj = runCatching { JSONObject(payload) }.getOrNull() ?: return null
        if (obj.has("error") || !obj.has("id")) return null
        return parsePlaylistObject(obj)
    }

    fun save(name: String, filters: ActiveFilters): Long {
        val req = JSONObject().apply {
            put("name", name)
            put("type", "search")
            put("filtersJson", filtersToJson(filters))
        }
        val result = RustCoreBridge.createPlaylist(userDbPath, req.toString())
        return runCatching { JSONObject(result).getLong("id") }.getOrDefault(0)
    }

    fun createManual(name: String): Long {
        val req = JSONObject().apply {
            put("name", name)
            put("type", "manual")
        }
        val result = RustCoreBridge.createPlaylist(userDbPath, req.toString())
        return runCatching { JSONObject(result).getLong("id") }.getOrDefault(0)
    }

    fun addTrack(playlistId: Long, trackId: Long) {
        RustCoreBridge.addTrackToPlaylist(userDbPath, playlistId, trackId)
    }

    fun removeTrack(playlistId: Long, trackId: Long) {
        RustCoreBridge.removeTrackFromPlaylist(userDbPath, playlistId, trackId)
    }

    fun rename(id: Long, newName: String) {
        RustCoreBridge.renamePlaylist(userDbPath, id, newName)
    }

    fun delete(id: Long) {
        RustCoreBridge.deletePlaylist(userDbPath, id)
    }

    fun parseFilters(entry: PlaylistEntry): ActiveFilters = jsonToFilters(entry.filtersJson)

    fun getManualPlaylists(): List<PlaylistEntry> {
        val payload = RustCoreBridge.getManualPlaylists(userDbPath)
        return parsePlaylistArray(payload)
    }

    private fun parsePlaylistArray(payload: String): List<PlaylistEntry> {
        val arr = runCatching { JSONArray(payload) }.getOrDefault(JSONArray())
        return buildList {
            for (i in 0 until arr.length()) {
                add(parsePlaylistObject(arr.getJSONObject(i)))
            }
        }
    }

    private fun parsePlaylistObject(obj: JSONObject): PlaylistEntry {
        val type = if (obj.optString("type") == "manual") PlaylistType.MANUAL else PlaylistType.SEARCH
        val trackIds = obj.optJSONArray("trackIds")?.let { a ->
            buildList { for (j in 0 until a.length()) add(a.getLong(j)) }
        } ?: emptyList()
        return PlaylistEntry(
            id = obj.optLong("id"),
            name = obj.optString("name", ""),
            type = type,
            filtersJson = obj.optString("filtersJson", "{}"),
            trackIds = trackIds,
            createdAt = obj.optLong("createdAt"),
        )
    }

    companion object {
        fun filtersToJson(filters: ActiveFilters): String {
            val obj = JSONObject()
            if (filters.transcriptQuery.isNotBlank()) obj.put("transcriptQuery", filters.transcriptQuery)
            fun putIds(key: String, ids: Set<Long>) { if (ids.isNotEmpty()) obj.put(key, JSONArray(ids.toList())) }
            fun putMatch(key: String, mode: MatchMode) { if (mode != MatchMode.Any) obj.put(key, mode.value) }
            putIds("categoryIds", filters.categoryIds)
            putIds("singerIds", filters.singerIds); putMatch("singerMatch", filters.singerMatch)
            putIds("modeIds", filters.modeIds); putMatch("modeMatch", filters.modeMatch)
            putIds("orchestraIds", filters.orchestraIds); putMatch("orchestraMatch", filters.orchestraMatch)
            putIds("instrumentIds", filters.instrumentIds); putMatch("instrumentMatch", filters.instrumentMatch)
            putIds("performerIds", filters.performerIds); putMatch("performerMatch", filters.performerMatch)
            putIds("poetIds", filters.poetIds); putMatch("poetMatch", filters.poetMatch)
            putIds("announcerIds", filters.announcerIds); putMatch("announcerMatch", filters.announcerMatch)
            putIds("composerIds", filters.composerIds); putMatch("composerMatch", filters.composerMatch)
            putIds("arrangerIds", filters.arrangerIds); putMatch("arrangerMatch", filters.arrangerMatch)
            putIds("orchestraLeaderIds", filters.orchestraLeaderIds); putMatch("orchestraLeaderMatch", filters.orchestraLeaderMatch)
            return obj.toString()
        }

        fun jsonToFilters(json: String): ActiveFilters {
            val obj = runCatching { JSONObject(json) }.getOrDefault(JSONObject())
            fun getIds(key: String): Set<Long> {
                val arr = obj.optJSONArray(key) ?: return emptySet()
                return buildSet { for (i in 0 until arr.length()) add(arr.getLong(i)) }
            }
            fun getMatch(key: String): MatchMode = if (obj.optString(key) == "all") MatchMode.All else MatchMode.Any
            return ActiveFilters(
                transcriptQuery = obj.optString("transcriptQuery", ""),
                categoryIds = getIds("categoryIds"),
                singerIds = getIds("singerIds"), singerMatch = getMatch("singerMatch"),
                modeIds = getIds("modeIds"), modeMatch = getMatch("modeMatch"),
                orchestraIds = getIds("orchestraIds"), orchestraMatch = getMatch("orchestraMatch"),
                instrumentIds = getIds("instrumentIds"), instrumentMatch = getMatch("instrumentMatch"),
                performerIds = getIds("performerIds"), performerMatch = getMatch("performerMatch"),
                poetIds = getIds("poetIds"), poetMatch = getMatch("poetMatch"),
                announcerIds = getIds("announcerIds"), announcerMatch = getMatch("announcerMatch"),
                composerIds = getIds("composerIds"), composerMatch = getMatch("composerMatch"),
                arrangerIds = getIds("arrangerIds"), arrangerMatch = getMatch("arrangerMatch"),
                orchestraLeaderIds = getIds("orchestraLeaderIds"), orchestraLeaderMatch = getMatch("orchestraLeaderMatch"),
            )
        }
    }
}
