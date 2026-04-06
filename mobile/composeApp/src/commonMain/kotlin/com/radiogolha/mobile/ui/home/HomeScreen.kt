package com.radiogolha.mobile.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.radiogolha.mobile.theme.GolhaAppTheme
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaSpacing

@Composable
fun HomeScreen(
    state: HomeUiState,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(GolhaColors.ScreenBackground),
            containerColor = GolhaColors.ScreenBackground,
            bottomBar = {
                BottomNavigationBar(items = state.bottomNavItems)
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 18.dp,
                    bottom = innerPadding.calculateBottomPadding() + 18.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(GolhaSpacing.SectionGap),
            ) {
                item { HeaderSection() }
                item { HeroBanner() }
                item { ProgramsSection(programs = state.programs) }
                item { SingersSection(singers = state.singers) }
                item { DastgahSection(items = state.dastgahs) }
                item { MusiciansSection(musicians = state.musicians) }
                item { TopTracksSection(tracks = state.topTracks) }
            }
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    GolhaAppTheme {
        HomeScreen(
            state = rememberSampleHomeUiState(),
        )
    }
}
