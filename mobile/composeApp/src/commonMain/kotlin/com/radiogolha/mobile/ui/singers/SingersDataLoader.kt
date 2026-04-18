package com.radiogolha.mobile.ui.singers

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.*
import kotlinx.serialization.decodeFromString

fun loadSingersUiState(): List<SingerListItemUiModel> {
    val payload = RustCoreBridge.getSingersJson(requireArchiveDbPath())
    val response = json.decodeFromString<List<SingerDto>>(payload)
    return response.map { 
        SingerListItemUiModel(
            artistId = it.id,
            name = it.name,
            imageUrl = it.avatar,
            programCount = 0 // Will need to update DTO if we want real count
        )
    }
}
