package com.radiogolha.mobile.ui.programs

import com.radiogolha.mobile.ui.home.ProgramUiModel

expect fun loadProgramsUiState(): List<ProgramUiModel>

expect fun loadCategoryPrograms(categoryTitle: String): List<com.radiogolha.mobile.ui.home.CategoryProgramUiModel>
expect fun loadProgramEpisodeDetail(programId: Long): com.radiogolha.mobile.ui.home.ProgramEpisodeDetailUiModel?
