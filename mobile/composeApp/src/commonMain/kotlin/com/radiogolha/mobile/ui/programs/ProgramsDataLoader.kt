package com.radiogolha.mobile.ui.programs

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.*
import kotlinx.serialization.decodeFromString

fun loadProgramsUiState(): List<ProgramUiModel> {
    val payload = RustCoreBridge.getHomeFeedJson(requireArchiveDbPath())
    val response = json.decodeFromString<HomeFeedResponse>(payload)
    return response.categories.map { ProgramUiModel(it.id, it.title, it.episodeCount) }
}

fun loadCategoryPrograms(categoryId: Long): List<CategoryProgramUiModel> {
    val payload = RustCoreBridge.getProgramsByCategoryJson(requireArchiveDbPath(), categoryId) 
    val response = json.decodeFromString<List<CategoryProgramDto>>(payload)
    return response.map { it.toCategoryProgramUiModel() }
}

fun loadProgramEpisodeDetail(programId: Long): ProgramEpisodeDetailUiModel {
    val payload = RustCoreBridge.getProgramDetailJson(requireArchiveDbPath(), programId)
    val response = json.decodeFromString<ProgramDetailResponse>(payload)
    
    return ProgramEpisodeDetailUiModel(
        id = response.id,
        title = response.title,
        categoryName = "نامشخص", // Needs info from DB
        no = 0, // Needs info from DB
        subNo = null,
        duration = response.duration,
        audioUrl = response.audioUrl,
        singers = response.singers.map { ArtistCreditUiModel(it.id, it.name, it.avatar) },
        poets = emptyList(),
        announcers = emptyList(),
        composers = response.composers.map { ArtistCreditUiModel(it.id, it.name, it.avatar) },
        arrangers = response.arrangers.map { ArtistCreditUiModel(it.id, it.name, it.avatar) },
        modes = emptyList(),
        orchestras = response.orchestras.map { ArtistCreditUiModel(it.id, it.name, it.avatar) },
        orchestraLeaders = emptyList(),
        performers = response.performers.map { PerformerUiModel(it.id, it.name, it.avatar, it.instrument) },
        timeline = emptyList(),
        transcript = emptyList()
    )
}
