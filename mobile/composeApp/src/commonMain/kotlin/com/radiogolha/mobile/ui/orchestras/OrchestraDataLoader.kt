package com.radiogolha.mobile.ui.orchestras

import com.radiogolha.mobile.ui.home.OrchestraListItemUiModel
import com.radiogolha.mobile.ui.home.CategoryProgramUiModel

expect fun loadOrchestrasUiState(): List<OrchestraListItemUiModel>

expect fun loadProgramsByOrchestra(orchestraId: Long): List<CategoryProgramUiModel>
