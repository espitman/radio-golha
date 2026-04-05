package com.radiogolha.mobile

import androidx.compose.runtime.Composable
import com.radiogolha.mobile.theme.GolhaAppTheme
import com.radiogolha.mobile.ui.home.HomeScreen
import com.radiogolha.mobile.ui.home.rememberSampleHomeUiState

@Composable
fun App() {
    GolhaAppTheme {
        HomeScreen(
            state = rememberSampleHomeUiState(),
        )
    }
}
