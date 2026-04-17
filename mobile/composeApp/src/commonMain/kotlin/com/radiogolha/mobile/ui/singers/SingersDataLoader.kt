package com.radiogolha.mobile.ui.singers

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.*
import kotlinx.serialization.decodeFromString

fun loadSingersUiState(): List<SingerListItemUiModel> {
    val payload = RustCoreBridge.getSingersJson(requireArchiveDbPath())
    val response = json.decodeFromString<List<SingerDto>>(payload)
    return response.map { 
        SingerListItemUiModel(
            id = it.id,
            artistId = it.id,
            name = it.name,
            imageUrl = it.avatar,
            trackCount = 0 // Will be handled if needed from API
        )
    }
}
