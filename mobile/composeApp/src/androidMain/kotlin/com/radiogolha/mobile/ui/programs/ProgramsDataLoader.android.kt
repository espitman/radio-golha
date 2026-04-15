package com.radiogolha.mobile.ui.programs

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.ProgramUiModel
import com.radiogolha.mobile.ui.home.requireArchiveDbPath
import org.json.JSONArray
import org.json.JSONObject

actual fun loadProgramsUiState(): List<ProgramUiModel> {
    // category_breakdown from Rust already sorted by total DESC
    val payload = runCatching { RustCoreBridge.getHomeFeedJson(requireArchiveDbPath()) }.getOrNull() ?: "{}"
    val root = if (payload.trim().startsWith("{")) JSONObject(payload) else JSONObject()
    return root.optJSONArray("programs")?.let { array ->
        buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(ProgramUiModel(
                    title = item.optString("title", ""),
                    episodeCount = extractCount(item),
                ))
            }
        }
    } ?: emptyList()
}

actual fun loadCategoryPrograms(categoryTitle: String): List<com.radiogolha.mobile.ui.home.CategoryProgramUiModel> {
    // 1. Get initial list from Categories JNI to find ID
    val catPayload = runCatching { RustCoreBridge.getCategoriesJson(requireArchiveDbPath()) }.getOrNull()
    val catRoot = if (catPayload != null && catPayload.trim().startsWith("[")) JSONArray(catPayload) else return emptyList()
    
    var categoryId: Long = -1
    for (i in 0 until catRoot.length()) {
        val cat = catRoot.getJSONObject(i)
        val title = cat.optString("titleFa").takeIf { it.isNotBlank() }
                    ?: cat.optString("titleFa").takeIf { it.isNotBlank() }
                    ?: cat.optString("title")
        if (title.trim().equals(categoryTitle.trim(), ignoreCase = true)) {
            categoryId = cat.optLong("id", -1)
            break
        }
    }

    if (categoryId == -1L) return emptyList()

    // 2. Call optimized JNI method
    val payload = runCatching { 
        RustCoreBridge.getProgramsByCategoryJson(requireArchiveDbPath(), categoryId) 
    }.getOrNull()
    
    val root = if (payload != null && payload.trim().startsWith("[")) JSONArray(payload) else return emptyList()
    
    return buildList {
        for (i in 0 until root.length()) {
            val item = root.getJSONObject(i)
            val no = item.optLong("no", 0).toString()
            val title = item.optNullableString("title") 
                ?: item.optNullableString("titleFa")
                ?: item.optNullableString("titleFa")
                ?: "$categoryTitle $no"
                
            add(
                com.radiogolha.mobile.ui.home.CategoryProgramUiModel(
                    id = item.optLong("id"),
                    artistId = item.optLong("artistId").takeIf { it > 0 } 
                        ?: item.optLong("artistId").takeIf { it > 0 },
                    title = title,
                    categoryName = categoryTitle,
                    programNumber = no,
                    singer = item.optString("artist", "ناشناس"),
                    duration = item.optNullableString("duration"),
                    dastgah = item.optNullableString("mode"),
                    audioUrl = item.optNullableString("audioUrl")
                )
            )
        }
    }
}

actual fun loadProgramEpisodeDetail(programId: Long): com.radiogolha.mobile.ui.home.ProgramEpisodeDetailUiModel? {
    val payload = runCatching { 
        RustCoreBridge.getProgramDetailJson(requireArchiveDbPath(), programId) 
    }.getOrNull() ?: return null
    
    if (payload.contains("\"error\"") && !payload.startsWith("{")) return null
    
    val root = runCatching { JSONObject(payload) }.getOrNull() ?: return null
    
    val singers = root.optArtistCredits("singers")
    val poets = root.optArtistCredits("poets")
    val announcers = root.optArtistCredits("announcers")
    val composers = root.optArtistCredits("composers")
    val arrangers = root.optArtistCredits("arrangers")
    val orchestras = root.optArtistCredits("orchestras")
    val orchestraLeaders = root.optOrchestraLeaders("orchestraLeaders").takeIf { it.isNotEmpty() } ?: root.optOrchestraLeaders("orchestra_leaders")
    val performers = root.optPerformers("performers").takeIf { it.isNotEmpty() } ?: root.optPerformers("program_performers")

    return com.radiogolha.mobile.ui.home.ProgramEpisodeDetailUiModel(
        id = root.optLong("id"),
        title = root.optString("title", ""),
        categoryName = root.optString("categoryName", ""),
        no = root.optInt("no", 0),
        subNo = root.optNullableString("subNo"),
        duration = root.optNullableString("duration"),
        audioUrl = root.optNullableString("audioUrl"),
        singers = singers,
        poets = poets,
        announcers = announcers,
        composers = composers,
        arrangers = arrangers,
        modes = root.optStringList("modes"),
        orchestras = orchestras,
        orchestraLeaders = orchestraLeaders,
        performers = performers,
        timeline = root.optTimeline("timeline"),
        transcript = root.optTranscript("transcript")
    )
}

private fun JSONObject.optArtistCredits(key: String): List<com.radiogolha.mobile.ui.home.ArtistCreditUiModel> {
    val array = optJSONArray(key) ?: return emptyList()
    return buildList {
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val id = item.optLong("artistId", 0L).takeIf { it > 0 }
                ?: item.optLong("id", 0L).takeIf { it > 0 }
                ?: item.optLong("performer_id", 0L).takeIf { it > 0 }
                ?: item.optLong("singer_id", 0L).takeIf { it > 0 }
                ?: item.optLong("musician_id", 0L).takeIf { it > 0 }
            add(
                com.radiogolha.mobile.ui.home.ArtistCreditUiModel(
                    artistId = id,
                    name = item.optString("name", ""),
                    avatar = item.optNullableString("avatar")
                )
            )
        }
    }
}

private fun JSONObject.optStringList(key: String): List<String> {
    val array = optJSONArray(key) ?: return emptyList()
    return buildList {
        for (i in 0 until array.length()) {
            val s = array.optString(i)
            if (s.isNotBlank()) add(s)
        }
    }
}

private fun JSONObject.optOrchestraLeaders(key: String): List<com.radiogolha.mobile.ui.home.OrchestraLeaderUiModel> {
    val array = optJSONArray(key) ?: return emptyList()
    return buildList {
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val id = item.optLong("artistId", 0L).takeIf { it > 0 }
                ?: item.optLong("id", 0L).takeIf { it > 0 }
                ?: item.optLong("performer_id", 0L).takeIf { it > 0 }
            add(
                com.radiogolha.mobile.ui.home.OrchestraLeaderUiModel(
                    artistId = id,
                    orchestra = item.optString("orchestra", ""),
                    name = item.optString("name", "")
                )
            )
        }
    }
}

private fun JSONObject.optPerformers(key: String): List<com.radiogolha.mobile.ui.home.PerformerUiModel> {
    val array = optJSONArray(key) ?: return emptyList()
    return buildList {
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val id = item.optLong("performer_id", 0L).takeIf { it > 0 }
                ?: item.optLong("artistId", 0L).takeIf { it > 0 }
                ?: item.optLong("id", 0L).takeIf { it > 0 }
            add(
                com.radiogolha.mobile.ui.home.PerformerUiModel(
                    artistId = id,
                    name = item.optString("name", ""),
                    avatar = item.optNullableString("avatar"),
                    instrument = item.optNullableString("instrument")
                )
            )
        }
    }
}

private fun JSONObject.optTimeline(key: String): List<com.radiogolha.mobile.ui.home.TimelineSegmentUiModel> {
    val array = optJSONArray(key) ?: return emptyList()
    return buildList {
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            add(
                com.radiogolha.mobile.ui.home.TimelineSegmentUiModel(
                    id = item.optLong("id"),
                    startTime = item.optNullableString("startTime") ?: item.optNullableString("start_time"),
                    endTime = item.optNullableString("endTime") ?: item.optNullableString("end_time"),
                    modeName = item.optNullableString("modeName") ?: item.optNullableString("mode_name"),
                    singers = item.optStringList("singers"),
                    poets = item.optStringList("poets"),
                    announcers = item.optStringList("announcers"),
                    orchestras = item.optStringList("orchestras"),
                    orchestraLeaders = item.optOrchestraLeaders("orchestraLeaders").takeIf { it.isNotEmpty() } ?: item.optOrchestraLeaders("orchestra_leaders"),
                    performers = item.optPerformers("performers").takeIf { it.isNotEmpty() } ?: item.optPerformers("program_performers")
                )
            )
        }
    }
}

private fun JSONObject.optTranscript(key: String): List<com.radiogolha.mobile.ui.home.TranscriptVerseUiModel> {
    val array = optJSONArray(key) ?: return emptyList()
    return buildList {
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            add(
                com.radiogolha.mobile.ui.home.TranscriptVerseUiModel(
                    segmentOrder = item.optInt("segmentOrder").takeIf { it > 0 } ?: item.optInt("segment_order"),
                    verseOrder = item.optInt("verseOrder").takeIf { it > 0 } ?: item.optInt("verse_order"),
                    text = item.optString("text", "")
                )
            )
        }
    }
}

private fun extractProgramNumberText(title: String): String {
    val regex = """[\d۰-۹]+""".toRegex()
    return regex.find(title)?.value ?: "0"
}

private fun extractCount(item: JSONObject): Int {
    val keys = listOf("episodeCount", "programCount", "itemCount", "trackCount", "total", "count")
    for (key in keys) {
        val c = item.optInt(key, 0)
        if (c > 0) return c
    }
    return 0
}

internal fun JSONObject.optNullableString(key: String): String? {
    if (isNull(key)) return null
    return optString(key)
        .trim()
        .takeUnless { it.isEmpty() || it.equals("null", ignoreCase = true) }
}
