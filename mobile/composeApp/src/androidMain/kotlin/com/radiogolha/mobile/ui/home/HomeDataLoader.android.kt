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
    val tracks = root.getJSONArray("topTracks").toTrackModels()

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

private const val CURRENT_DB_VERSION = 2

internal fun requireArchiveDbPath(): String {
    val context = AndroidAppContext.require()
    val dbFile = File(context.filesDir, "golha_database.db")
    val versionFile = File(context.filesDir, "golha_db_version")

    val installedVersion = if (versionFile.exists()) versionFile.readText().trim().toIntOrNull() ?: 0 else 0

    if (!dbFile.exists() || installedVersion < CURRENT_DB_VERSION) {
        context.assets.open("golha_database.db").use { input ->
            dbFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        versionFile.writeText(CURRENT_DB_VERSION.toString())
    }

    return dbFile.absolutePath
}

private fun JSONArray.toProgramModels(): List<ProgramUiModel> = buildList {
    for (index in 0 until length()) {
        val item = getJSONObject(index)
        add(
            ProgramUiModel(
                title = item.optString("title", ""),
                episodeCount = item.optInt("episodeCount", 0),
            )
        )
    }
}

private fun JSONArray.toSingerModels(): List<SingerUiModel> = buildList {
    for (index in 0 until length()) {
        val item = getJSONObject(index)
        add(
            SingerUiModel(
                id = item.optLong("id", 0L),
                name = item.optString("name", ""),
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
                id = item.optLong("id", 0L),
                name = item.optString("name", ""),
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
                artistId = item.optLong("artistId").takeIf { it > 0 },
                title = item.optString("title", "بدون عنوان"),
                artist = item.getString("artist").split(" - ")[0],
                duration = item.getString("duration"),
                audioUrl = item.optNullableString("audioUrl"),
                coverUrl = avatar,
                artistImages = if (avatar != null) listOf(avatar) else emptyList()
            )
        )
    }
}

actual fun loadProgramsByIds(ids: List<Long>): List<CategoryProgramUiModel> {
    if (ids.isEmpty()) return emptyList()
    val payload = RustCoreBridge.getProgramsByIdsJson(requireArchiveDbPath(), org.json.JSONArray(ids).toString())
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
                audioUrl = item.optNullableString("audioUrl"),
            ))
        }
    }
}

actual fun loadProgramsByMode(modeId: Long): List<CategoryProgramUiModel> {
    val payload = RustCoreBridge.getProgramsByModeJson(requireArchiveDbPath(), modeId)
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
                audioUrl = item.optNullableString("audioUrl"),
            ))
        }
    }
}

actual fun loadDuetPairsConfig(): List<DuetPairUiModel> {
    val payload = RustCoreBridge.getDuetPairsConfigJson(requireArchiveDbPath())
    val arr = JSONArray(payload)
    return buildList {
        for (i in 0 until arr.length()) {
            val item = arr.getJSONObject(i)
            add(DuetPairUiModel(
                singer1 = item.getString("singer1"),
                singer2 = item.getString("singer2"),
                singer1Avatar = item.optNullableString("singer1Avatar"),
                singer2Avatar = item.optNullableString("singer2Avatar"),
                trackCount = item.optInt("trackCount", 0),
            ))
        }
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
                audioUrl = item.optNullableString("audioUrl"),
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
