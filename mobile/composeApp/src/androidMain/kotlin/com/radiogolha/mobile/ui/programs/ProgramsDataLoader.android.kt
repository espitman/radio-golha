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
            add(
                com.radiogolha.mobile.ui.home.CategoryProgramUiModel(
                    id = item.optLong("id"),
                    programNumber = item.optLong("no", 0).toString(),
                    singer = item.optString("artist", "ناشناس"),
                    duration = item.optNullableString("duration"),
                    dastgah = item.optNullableString("mode"),
                    audioUrl = item.optNullableString("audio_url")
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
