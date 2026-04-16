package com.radiogolha.mobile.ui.home

actual fun loadHomeUiState(): HomeUiState? = null
actual fun loadTopTracks(): List<TrackUiModel> = emptyList()
actual fun loadDuetPrograms(singer1: String, singer2: String): List<CategoryProgramUiModel> = emptyList()
actual fun loadProgramsByIds(ids: List<Long>): List<CategoryProgramUiModel> = emptyList()
actual fun loadProgramsByMode(modeId: Long): List<CategoryProgramUiModel> = emptyList()
actual fun loadDuetPairsConfig(): List<DuetPairUiModel> = emptyList()
actual fun loadOrderedModes(): List<String> = emptyList()
