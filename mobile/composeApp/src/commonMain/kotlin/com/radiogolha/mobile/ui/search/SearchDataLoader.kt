package com.radiogolha.mobile.ui.search

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.*
import com.radiogolha.mobile.ui.home.json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

fun loadSearchOptions(): SearchOptionsUiState {
    val payload = RustCoreBridge.getSearchOptionsJson(requireArchiveDbPath())
    val response = json.decodeFromString<SearchOptionsResponse>(payload)
    
    fun List<SearchOptionDto>.toModel() = map { 
        SearchOptionUiModel(id = it.id, name = it.name ?: it.titleFa ?: "") 
    }.filter { it.name.isNotBlank() }

    return SearchOptionsUiState(
        categories = response.categories.toModel(),
        singers = response.singers.toModel(),
        modes = response.modes.toModel(),
        orchestras = response.orchestras.toModel(),
        instruments = response.instruments.toModel(),
        performers = response.performers.toModel(),
        poets = response.poets.toModel(),
        announcers = response.announcers.toModel(),
        composers = response.composers.toModel(),
        arrangers = response.arrangers.toModel(),
        orchestraLeaders = (response.orchestraLeaders.takeIf { it.isNotEmpty() } ?: response.orchestra_leaders).toModel()
    )
}

fun searchPrograms(filters: ActiveFilters, page: Int): SearchResultsUiState {
    val filtersDto = SearchFiltersDto(
        transcriptQuery = filters.transcriptQuery.takeIf { it.isNotBlank() },
        page = page,
        categoryIds = filters.categoryIds.toList(),
        singerIds = filters.singerIds.toList(),
        singerMatch = filters.singerMatch.value,
        modeIds = filters.modeIds.toList(),
        modeMatch = filters.modeMatch.value,
        orchestraIds = filters.orchestraIds.toList(),
        orchestraMatch = filters.orchestraMatch.value,
        instrumentIds = filters.instrumentIds.toList(),
        instrumentMatch = filters.instrumentMatch.value,
        performerIds = filters.performerIds.toList(),
        performerMatch = filters.performerMatch.value,
        poetIds = filters.poetIds.toList(),
        poetMatch = filters.poetMatch.value,
        announcerIds = filters.announcerIds.toList(),
        announcerMatch = filters.announcerMatch.value,
        composerIds = filters.composerIds.toList(),
        composerMatch = filters.composerMatch.value,
        arrangerIds = filters.arrangerIds.toList(),
        arrangerMatch = filters.arrangerMatch.value,
        orchestraLeaderIds = filters.orchestraLeaderIds.toList(),
        orchestraLeaderMatch = filters.orchestraLeaderMatch.value
    )
    
    val filtersJson = json.encodeToString(filtersDto)
    val payload = RustCoreBridge.searchProgramsJson(requireArchiveDbPath(), filtersJson)
    val response = json.decodeFromString<SearchResultsResponse>(payload)
    
    return SearchResultsUiState(
        results = response.rows.map { 
            SearchResultUiModel(
                id = it.id,
                title = it.title,
                categoryName = it.categoryName,
                no = it.no,
                subNo = it.subNo,
                duration = it.duration,
                audioUrl = it.audioUrl,
                artist = it.artist
            )
        },
        total = response.total,
        page = response.page,
        totalPages = response.total_pages ?: response.totalPages
    )
}
