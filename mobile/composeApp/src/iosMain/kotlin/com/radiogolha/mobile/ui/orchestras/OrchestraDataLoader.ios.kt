package com.radiogolha.mobile.ui.orchestras

import com.radiogolha.mobile.ui.home.OrchestraListItemUiModel
import com.radiogolha.mobile.ui.home.CategoryProgramUiModel

actual fun loadOrchestrasUiState(): List<OrchestraListItemUiModel> = emptyList()

actual fun loadProgramsByOrchestra(orchestraId: Long): List<CategoryProgramUiModel> = emptyList()
