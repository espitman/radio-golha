package com.radiogolha.mobile.ui.home

import com.radiogolha.mobile.AndroidAppContext
import com.radiogolha.mobile.RustCoreBridge
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

actual fun loadHomeUiState(): HomeUiState? {
    val payload = RustCoreBridge.getHomeFeedJson(requireArchiveDbPath())
    if (payload.trim().startsWith("{")) {
        val error = JSONObject(payload).optString("error")
        if (error.isNotBlank()) {
            throw IllegalStateException(error)
        }
    }

    val root = JSONObject(payload)
    val programs = root.getJSONArray("programs").toProgramModels()
    val singers = root.getJSONArray("singers").toSingerModels()
    val dastgahs = root.getJSONArray("dastgahs").toDastgahModels()
    val musicians = root.getJSONArray("musicians").toMusicianModels()
    val tracks = root.getJSONArray("top_tracks").toTrackModels()

    return HomeUiState(
        programs = programs,
        singers = singers,
        dastgahs = dastgahs,
        musicians = musicians,
        topTracks = tracks,
        bottomNavItems = emptyList(),
    )
}

actual fun loadTopTracks(): List<TrackUiModel> {
    val payload = RustCoreBridge.getTopTracksJson(requireArchiveDbPath())
    if (payload.trim().startsWith("{")) {
        val error = JSONObject(payload).optString("error")
        if (error.isNotBlank()) {
            throw IllegalStateException(error)
        }
    }

    return JSONArray(payload).toTrackModels()
}

internal fun requireArchiveDbPath(): String {
    val context = AndroidAppContext.require()
    val dbFile = File(context.filesDir, "golha_database.db")

    if (!dbFile.exists()) {
        context.assets.open("golha_database.db").use { input ->
            dbFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    return dbFile.absolutePath
}

private fun JSONArray.toProgramModels(): List<ProgramUiModel> = buildList {
    for (index in 0 until length()) {
        val item = getJSONObject(index)
        add(
            ProgramUiModel(
                title = item.optString("title").takeIf { it.isNotBlank() } 
                        ?: item.optString("titleFa").takeIf { it.isNotBlank() }
                        ?: item.optString("title_fa").takeIf { it.isNotBlank() }
                        ?: "برنامه بدون نام",
                episodeCount = item.optInt("episodeCount", 0).takeIf { it > 0 } ?: item.optInt("episode_count", 0),
            )
        )
    }
}

private fun JSONArray.toSingerModels(): List<SingerUiModel> = buildList {
    for (index in 0 until length()) {
        val item = getJSONObject(index)
        add(
            SingerUiModel(
                id = item.optLong("id").takeIf { it > 0 } 
                    ?: item.optLong("artist_id").takeIf { it > 0 }
                    ?: item.optLong("artistId", 0L),
                name = item.optString("name", "نامعلوم"),
                imageUrl = item.optNullableString("avatar"),
            )
        )
    }
}

private fun JSONArray.toDastgahModels(): List<DastgahUiModel> = buildList {
    for (index in 0 until length()) {
        add(DastgahUiModel(name = getJSONObject(index).getString("name")))
    }
}

private fun JSONArray.toMusicianModels(): List<MusicianUiModel> = buildList {
    for (index in 0 until length()) {
        val item = getJSONObject(index)
        add(
            MusicianUiModel(
                id = item.optLong("id").takeIf { it > 0 } 
                    ?: item.optLong("artist_id").takeIf { it > 0 }
                    ?: item.optLong("artistId", 0L),
                name = item.optString("name", "نامعلوم"),
                instrument = item.optString("instrument", ""),
                imageUrl = item.optNullableString("avatar"),
            )
        )
    }
}

internal fun JSONArray.toTrackModels(): List<TrackUiModel> = buildList {
    for (index in 0 until length()) {
        val item = getJSONObject(index)
        val avatar = item.optNullableString("avatar") ?: item.optNullableString("cover")
        add(
            TrackUiModel(
                id = item.optLong("id", 0L),
                artistId = item.optLong("artist_id").takeIf { it > 0 } ?: item.optLong("artistId").takeIf { it > 0 },
                title = item.optString("title", "بدون عنوان"),
                artist = item.getString("artist").split(" - ")[0],
                duration = item.getString("duration"),
                audioUrl = item.optNullableString("audio_url"),
                coverUrl = avatar,
                artistImages = if (avatar != null) listOf(avatar) else emptyList()
            )
        )
    }
}

actual fun loadOrderedModes(): List<String> {
    val payload = RustCoreBridge.getOrderedModesJson(requireArchiveDbPath())
    val arr = JSONArray(payload)
    return buildList { for (i in 0 until arr.length()) add(arr.getString(i)) }
}

actual fun loadDuetPrograms(singer1: String, singer2: String): List<CategoryProgramUiModel> {
    val payload = RustCoreBridge.getDuetProgramsJson(requireArchiveDbPath(), singer1, singer2)
    val root = JSONArray(payload)
    return buildList {
        for (i in 0 until root.length()) {
            val item = root.getJSONObject(i)
            add(CategoryProgramUiModel(
                id = item.optLong("id"),
                title = item.optNullableString("title") ?: "برنامه ${item.optLong("no")}",
                categoryName = null,
                programNumber = item.optLong("no", 0).toString(),
                singer = item.optString("artist", "ناشناس"),
                duration = item.optNullableString("duration"),
                dastgah = item.optNullableString("mode"),
                audioUrl = item.optNullableString("audio_url"),
            ))
        }
    }
}

internal fun JSONObject.optNullableString(key: String): String? {
    if (isNull(key)) return null
    return optString(key)
        .trim()
        .takeUnless { it.isEmpty() || it.equals("null", ignoreCase = true) }
}
