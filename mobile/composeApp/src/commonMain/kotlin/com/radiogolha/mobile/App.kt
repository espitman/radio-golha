package com.radiogolha.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.radiogolha.mobile.theme.GolhaAppTheme
import com.radiogolha.mobile.ui.home.HomeScreen
import com.radiogolha.mobile.ui.home.loadHomeUiState
import com.radiogolha.mobile.ui.home.sampleHomeUiState

@Composable
fun App() {
    val state = remember {
        runCatching { loadHomeUiState() }
            .getOrNull()
            ?: sampleHomeUiState()
    }

    GolhaAppTheme {
        HomeScreen(
            state = state,
        )
    }
}
