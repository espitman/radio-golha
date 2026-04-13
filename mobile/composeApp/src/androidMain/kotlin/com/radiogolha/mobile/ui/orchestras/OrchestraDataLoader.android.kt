package com.radiogolha.mobile.ui.orchestras

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.CategoryProgramUiModel
import com.radiogolha.mobile.ui.home.OrchestraListItemUiModel
import com.radiogolha.mobile.ui.home.requireArchiveDbPath
import com.radiogolha.mobile.ui.home.optNullableString
import org.json.JSONArray

actual fun loadOrchestrasUiState(): List<OrchestraListItemUiModel> {
    return try {
        val payload = RustCoreBridge.getOrchestrasJson(requireArchiveDbPath())
        println("DEBUG: Orchestras payload length=${payload.length}, first100=${payload.take(100)}")
        val root = JSONArray(payload)
        val result = buildList {
            for (index in 0 until root.length()) {
                val item = root.getJSONObject(index)
                add(
                    OrchestraListItemUiModel(
                        id = item.getLong("id"),
                        name = item.getString("name"),
                        programCount = item.getInt("program_count"),
                    )
                )
            }
        }
        println("DEBUG: Loaded ${result.size} orchestras for Library")
        result
    } catch (e: Throwable) {
        println("DEBUG: Failed to load orchestras: ${e.javaClass.name}: ${e.message}")
        e.printStackTrace()
        emptyList()
    }
}

actual fun loadProgramsByOrchestra(orchestraId: Long): List<CategoryProgramUiModel> {
    val payload = RustCoreBridge.getProgramsByOrchestraJson(requireArchiveDbPath(), orchestraId)
    val root = JSONArray(payload)
    return buildList {
        for (i in 0 until root.length()) {
            val item = root.getJSONObject(i)
            val no = item.optLong("no", 0).toString()
            add(
                CategoryProgramUiModel(
                    id = item.optLong("id"),
                    title = item.optNullableString("title") ?: "برنامه $no",
                    categoryName = null,
                    programNumber = no,
                    singer = item.optString("artist", "ناشناس"),
                    duration = item.optNullableString("duration"),
                    dastgah = item.optNullableString("mode"),
                    audioUrl = item.optNullableString("audio_url"),
                )
            )
        }
    }
}
