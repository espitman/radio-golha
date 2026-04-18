package com.radiogolha.mobile.ui.musicians

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.*
import kotlinx.serialization.decodeFromString

fun loadMusiciansUiState(): List<MusicianListItemUiModel> {
    val payload = RustCoreBridge.getMusiciansJson(requireArchiveDbPath())
    val response = json.decodeFromString<List<MusicianDto>>(payload)
    return response.map { 
        MusicianListItemUiModel(
            artistId = it.id,
            name = it.name,
            instrument = it.instrument,
            imageUrl = it.avatar,
            programCount = it.programCount
        )
    }
}
