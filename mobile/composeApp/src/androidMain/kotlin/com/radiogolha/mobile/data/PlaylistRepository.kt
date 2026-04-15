package com.radiogolha.mobile.data

import android.content.Context
import android.content.SharedPreferences
import com.radiogolha.mobile.ui.search.ActiveFilters
import com.radiogolha.mobile.ui.search.MatchMode
import org.json.JSONArray
import org.json.JSONObject

data class PlaylistEntry(
    val id: Long,
    val name: String,
    val filtersJson: String,
    val createdAt: Long,
)

class PlaylistRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("playlists", Context.MODE_PRIVATE)

    fun getAll(): List<PlaylistEntry> {
        val json = prefs.getString("items", null) ?: return emptyList()
        val arr = runCatching { JSONArray(json) }.getOrDefault(JSONArray())
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                add(PlaylistEntry(
                    id = obj.optLong("id"),
                    name = obj.optString("name", ""),
                    filtersJson = obj.optString("filtersJson", "{}"),
                    createdAt = obj.optLong("createdAt"),
                ))
            }
        }.sortedByDescending { it.createdAt }
    }

    fun getById(id: Long): PlaylistEntry? = getAll().find { it.id == id }

    fun save(name: String, filters: ActiveFilters): Long {
        val all = getAll().toMutableList()
        val id = (all.maxOfOrNull { it.id } ?: 0) + 1
        all.add(PlaylistEntry(id = id, name = name, filtersJson = filtersToJson(filters), createdAt = System.currentTimeMillis()))
        persist(all)
        return id
    }

    fun delete(id: Long) {
        val all = getAll().filter { it.id != id }
        persist(all)
    }

    fun parseFilters(entry: PlaylistEntry): ActiveFilters = jsonToFilters(entry.filtersJson)

    private fun persist(items: List<PlaylistEntry>) {
        val arr = JSONArray()
        items.forEach { entry ->
            arr.put(JSONObject().apply {
                put("id", entry.id)
                put("name", entry.name)
                put("filtersJson", entry.filtersJson)
                put("createdAt", entry.createdAt)
            })
        }
        prefs.edit().putString("items", arr.toString()).apply()
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
