package com.radiogolha.mobile.ui.home

expect fun loadHomeUiState(): HomeUiState?
expect fun loadTopTracks(): List<TrackUiModel>
expect fun loadDuetPrograms(singer1: String, singer2: String): List<CategoryProgramUiModel>
expect fun loadProgramsByIds(ids: List<Long>): List<CategoryProgramUiModel>
expect fun loadOrderedModes(): List<String>
expect fun loadDuetPairsConfig(): List<DuetPairUiModel>
expect fun loadProgramsByMode(modeId: Long): List<CategoryProgramUiModel>
