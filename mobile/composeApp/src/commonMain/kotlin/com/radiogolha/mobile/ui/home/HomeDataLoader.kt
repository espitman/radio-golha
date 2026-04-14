package com.radiogolha.mobile.ui.home

expect fun loadHomeUiState(): HomeUiState?
expect fun loadTopTracks(): List<TrackUiModel>
expect fun loadDuetPrograms(singer1: String, singer2: String): List<CategoryProgramUiModel>
