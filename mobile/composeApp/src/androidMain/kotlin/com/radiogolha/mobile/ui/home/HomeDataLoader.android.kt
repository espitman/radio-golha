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
                title = item.getString("title"),
                episodeCount = item.getInt("episode_count"),
            )
        )
    }
}

private fun JSONArray.toSingerModels(): List<SingerUiModel> = buildList {
    for (index in 0 until length()) {
        val item = getJSONObject(index)
        add(
            SingerUiModel(
                name = item.getString("name"),
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
                name = item.getString("name"),
                instrument = item.getString("instrument"),
                imageUrl = item.optNullableString("avatar"),
            )
        )
    }
}

internal fun JSONArray.toTrackModels(): List<TrackUiModel> = buildList {
    for (index in 0 until length()) {
        val item = getJSONObject(index)
        add(
            TrackUiModel(
                title = item.getString("title"),
                artist = item.getString("artist"),
                duration = item.getString("duration"),
            )
        )
    }
}

internal fun JSONObject.optNullableString(key: String): String? {
    if (isNull(key)) return null
    return optString(key)
        .trim()
        .takeUnless { it.isEmpty() || it.equals("null", ignoreCase = true) }
}
