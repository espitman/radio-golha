package com.radiogolha.mobile.ui.artists

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.ArtistDetailUiModel
import com.radiogolha.mobile.ui.home.CategoryProgramUiModel
import com.radiogolha.mobile.ui.home.optNullableString
import com.radiogolha.mobile.ui.home.requireArchiveDbPath
import org.json.JSONArray
import org.json.JSONObject

actual fun loadArtistDetail(artistId: Long): ArtistDetailUiModel? {
    val payload = RustCoreBridge.getArtistDetailJson(requireArchiveDbPath(), artistId)
    val root = runCatching { JSONObject(payload) }.getOrNull() ?: return null
    if (root.has("error")) return null

    return ArtistDetailUiModel(
        artistId = root.getLong("id"),
        name = root.getString("name"),
        imageUrl = root.optNullableString("avatar"),
        instrument = root.optNullableString("instrument"),
        trackCount = root.optInt("trackCount"),
        tracks = root.optJSONArray("tracks").toCategoryPrograms(root.getLong("id")),
    )
}

private fun JSONArray?.toCategoryPrograms(artistId: Long): List<CategoryProgramUiModel> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val item = getJSONObject(index)
            val title = item.optNullableString("title")
                ?: "برنامه ${item.optLong("no", 0)}"
            val no = item.optLong("no", 0).toString()
            add(
                CategoryProgramUiModel(
                    id = item.optLong("id"),
                    artistId = artistId,
                    title = title,
                    categoryName = item.optNullableString("category"),
                    programNumber = no,
                    singer = item.optString("artist", "ناشناس"),
                    duration = item.optNullableString("duration"),
                    dastgah = item.optNullableString("mode"),
                    audioUrl = item.optNullableString("audioUrl"),
                )
            )
        }
    }
}
