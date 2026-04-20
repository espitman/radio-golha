package com.radiogolha.mobile.ui.singers

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.*
import kotlinx.serialization.decodeFromString

fun loadSingersUiState(): List<SingerListItemUiModel> {
    return try {
        val payload = RustCoreBridge.getSingersJson(requireArchiveDbPath())
        val response = json.decodeFromString<List<SingerDto>>(payload)
        response.map { 
            SingerListItemUiModel(
                artistId = it.id,
                name = it.name,
                imageUrl = it.avatar,
                programCount = it.programCount
            )
        }
    } catch (e: Exception) {
        println("ERROR loading singers: ${e.message}")
        emptyList()
    }
}
