package com.radiogolha.mobile.ui.singers

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.SingerListItemUiModel
import com.radiogolha.mobile.ui.home.optNullableString
import com.radiogolha.mobile.ui.home.requireArchiveDbPath
import org.json.JSONArray

actual fun loadSingersUiState(): List<SingerListItemUiModel> {
    val payload = RustCoreBridge.getSingersJson(requireArchiveDbPath())
    val root = JSONArray(payload)
    return buildList {
        for (index in 0 until root.length()) {
            val item = root.getJSONObject(index)
            add(
                SingerListItemUiModel(
                    artistId = item.getLong("id"),
                    name = item.getString("name"),
                    imageUrl = item.optNullableString("avatar"),
                    programCount = item.getInt("programCount"),
                )
            )
        }
    }
}
