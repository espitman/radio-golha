package com.radiogolha.mobile.ui.orchestras

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.*
import kotlinx.serialization.decodeFromString

fun loadOrchestrasUiState(): List<OrchestraListItemUiModel> {
    val payload = RustCoreBridge.getOrchestrasJson(requireArchiveDbPath())
    val response = json.decodeFromString<List<SingerDto>>(payload)
    return response.map { 
        OrchestraListItemUiModel(
            id = it.id,
            name = it.name,
            programCount = 0 // Will need to update DTO if we want real count
        )
    }
}

fun loadProgramsByOrchestra(orchestraId: Long): List<CategoryProgramUiModel> {
    val payload = RustCoreBridge.getProgramsByOrchestraJson(requireArchiveDbPath(), orchestraId)
    val response = json.decodeFromString<List<CategoryProgramDto>>(payload)
    return response.map { it.toCategoryProgramUiModel() }
}
