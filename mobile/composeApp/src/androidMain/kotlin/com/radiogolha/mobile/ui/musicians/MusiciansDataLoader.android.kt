package com.radiogolha.mobile.ui.musicians

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.MusicianListItemUiModel
import com.radiogolha.mobile.ui.home.optNullableString
import com.radiogolha.mobile.ui.home.requireArchiveDbPath
import org.json.JSONArray

actual fun loadMusiciansUiState(): List<MusicianListItemUiModel> {
    val payload = RustCoreBridge.getMusiciansJson(requireArchiveDbPath())
    val root = JSONArray(payload)
    return buildList {
        for (index in 0 until root.length()) {
            val item = root.getJSONObject(index)
            add(
                MusicianListItemUiModel(
                    name = item.getString("name"),
                    instrument = item.getString("instrument"),
                    imageUrl = item.optNullableString("avatar"),
                    programCount = item.getInt("program_count"),
                )
            )
        }
    }
}
