package com.radiogolha.mobile.ui.programs

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.*
import kotlinx.serialization.decodeFromString

fun loadProgramsUiState(): List<ProgramUiModel> {
    val payload = RustCoreBridge.getHomeFeedJson(requireArchiveDbPath())
    val response = json.decodeFromString<HomeFeedResponse>(payload)
    return response.categories.map { ProgramUiModel(it.title, it.episodeCount) }
}

fun loadCategoryPrograms(categoryTitle: String): List<CategoryProgramUiModel> {
    // We need to find the ID for the title first if using getProgramsByCategoryJson
    // But Android implementation was using title directly? No, search showed requireArchiveDbPath
    val payload = RustCoreBridge.getProgramsByCategoryJson(requireArchiveDbPath(), categoryTitle) 
    val response = json.decodeFromString<List<CategoryProgramDto>>(payload)
    return response.map { it.toCategoryProgramUiModel() }
}

fun loadProgramDetail(programId: Long): ProgramDetailUiState {
    val payload = RustCoreBridge.getProgramDetailJson(requireArchiveDbPath(), programId)
    val response = json.decodeFromString<ProgramDetailResponse>(payload)
    
    return ProgramDetailUiState(
        id = response.id,
        title = response.title,
        duration = response.duration,
        audioUrl = response.audioUrl,
        description = response.description,
        singers = response.singers.map { SingerUiModel(it.id, it.name, it.avatar) },
        performers = response.performers.map { MusicianUiModel(it.id, it.name, it.instrument, it.avatar) },
        composers = response.composers.map { SingerUiModel(it.id, it.name, it.avatar) },
        arrangers = response.arrangers.map { SingerUiModel(it.id, it.name, it.avatar) },
        orchestras = response.orchestras.map { SingerUiModel(it.id, it.name, it.avatar) }
    )
}
