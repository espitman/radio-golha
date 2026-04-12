package com.radiogolha.mobile.ui.programs

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.ProgramUiModel
import com.radiogolha.mobile.ui.home.requireArchiveDbPath
import org.json.JSONArray
import org.json.JSONObject

actual fun loadProgramsUiState(): List<ProgramUiModel> {
    // 1. Get initial list from Categories JNI
    val payload = runCatching { RustCoreBridge.getCategoriesJson(requireArchiveDbPath()) }.getOrNull()
    val root = if (payload != null && payload.trim().startsWith("[")) JSONArray(payload) else null
    
    val categories = if (root != null) {
        buildList {
            for (index in 0 until root.length()) {
                val item = root.getJSONObject(index)
                add(
                    ProgramUiModel(
                        title = item.optString("titleFa").takeIf { it.isNotBlank() }
                                ?: item.optString("title_fa").takeIf { it.isNotBlank() }
                                ?: item.optString("title").takeIf { it.isNotBlank() }
                                ?: "برنامه بدون نام",
                        episodeCount = extractCount(item),
                    )
                )
            }
        }
    } else emptyList()
    
    // 2. Load from home feed to get programs with counts
    val homePayload = runCatching { RustCoreBridge.getHomeFeedJson(requireArchiveDbPath()) }.getOrNull() ?: "{}"
    val homeRoot = if (homePayload.trim().startsWith("{")) JSONObject(homePayload) else JSONObject()
    val homePrograms = homeRoot.optJSONArray("programs")?.let { array ->
        buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(
                    ProgramUiModel(
                        title = item.optString("title").takeIf { it.isNotBlank() } 
                                ?: item.optString("titleFa").takeIf { it.isNotBlank() }
                                ?: item.optString("title_fa").takeIf { it.isNotBlank() }
                                ?: "برنامه بدون نام",
                        episodeCount = extractCount(item)
                    )
                )
            }
        }
    } ?: emptyList()

    // 3. Merge: Use categories if available, otherwise homePrograms
    val baseList = if (categories.isEmpty()) homePrograms else categories

    // Merge: If base item has 0 count, try to find it in home programs
    val merged = baseList.map { item ->
        if (item.episodeCount > 0) item
        else {
            val fromHome = homePrograms.find { it.title.trim().equals(item.title.trim(), ignoreCase = true) }
            fromHome ?: item
        }
    }

    // Final sorting: By episodeCount Descending, then by Title
    val result = merged.sortedWith(
        compareByDescending<ProgramUiModel> { it.episodeCount }
            .thenBy { it.title }
    )
    println("DEBUG: Loaded ${result.size} programs for Library")
    return result
}

actual fun loadCategoryPrograms(categoryTitle: String): List<com.radiogolha.mobile.ui.home.CategoryProgramUiModel> {
    // 1. Get initial list from Categories JNI to find ID
    val catPayload = runCatching { RustCoreBridge.getCategoriesJson(requireArchiveDbPath()) }.getOrNull()
    val catRoot = if (catPayload != null && catPayload.trim().startsWith("[")) JSONArray(catPayload) else return emptyList()
    
    var categoryId: Long = -1
    for (i in 0 until catRoot.length()) {
        val cat = catRoot.getJSONObject(i)
        val title = cat.optString("titleFa").takeIf { it.isNotBlank() }
                    ?: cat.optString("title_fa").takeIf { it.isNotBlank() }
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
                ?: item.optNullableString("title_fa")
                ?: item.optNullableString("titleFa")
                ?: "$categoryTitle $no"
                
            add(
                com.radiogolha.mobile.ui.home.CategoryProgramUiModel(
                    id = item.optLong("id"),
                    artistId = item.optLong("artist_id").takeIf { it > 0 } 
                        ?: item.optLong("artistId").takeIf { it > 0 },
                    title = title,
                    categoryName = categoryTitle,
                    programNumber = no,
                    singer = item.optString("artist", "ناشناس"),
                    duration = item.optNullableString("duration"),
                    dastgah = item.optNullableString("mode"),
                    audioUrl = item.optNullableString("audio_url")
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
        categoryName = root.optString("categoryName") ?: root.optString("category_name") ?: "",
        no = root.optInt("no", 0),
        subNo = root.optNullableString("subNo") ?: root.optNullableString("sub_no"),
        duration = root.optNullableString("duration"),
        audioUrl = root.optNullableString("audioUrl") ?: root.optNullableString("audio_url"),
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
            val id = item.optLong("artist_id", 0L).takeIf { it > 0 }
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
            val id = item.optLong("artist_id", 0L).takeIf { it > 0 }
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
                ?: item.optLong("artist_id", 0L).takeIf { it > 0 }
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
    val keys = listOf(
        "episodeCount", "episode_count",
        "programCount", "program_count",
        "itemCount", "item_count",
        "trackCount", "track_count",
        "total", "count",
        "programs", "episodes"
    )
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
