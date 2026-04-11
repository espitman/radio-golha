package com.radiogolha.mobile.ui.programs

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.ProgramUiModel
import com.radiogolha.mobile.ui.home.requireArchiveDbPath
import org.json.JSONArray
import org.json.JSONObject

actual fun loadProgramsUiState(): List<ProgramUiModel> {
    // We'll try Categories first, if it fails or is empty, we fall back to home feed programs
    val payload = RustCoreBridge.getCategoriesJson(requireArchiveDbPath())
    val root = if (payload.trim().startsWith("[")) JSONArray(payload) else null
    
    if (root != null && root.length() > 0) {
        return buildList {
            for (index in 0 until root.length()) {
                val item = root.getJSONObject(index)
                add(
                    ProgramUiModel(
                        title = item.getString("title"),
                        episodeCount = item.getInt("episode_count"),
                    )
                )
            }
        }
    }
    
    // Fallback: load from home feed
    val homePayload = RustCoreBridge.getHomeFeedJson(requireArchiveDbPath())
    val homeRoot = JSONObject(homePayload)
    val programsArray = homeRoot.getJSONArray("programs")
    return buildList {
        for (index in 0 until programsArray.length()) {
            val item = programsArray.getJSONObject(index)
            add(
                ProgramUiModel(
                    title = item.getString("title"),
                    episodeCount = item.getInt("episode_count"),
                )
            )
        }
    }
}
