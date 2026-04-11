package com.radiogolha.mobile.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.*
import com.radiogolha.mobile.ui.programs.ProgramsScreen
import com.radiogolha.mobile.ui.singers.SingersContent
import com.radiogolha.mobile.ui.musicians.MusiciansContent
import kotlinx.coroutines.launch

enum class LibraryTab(val title: String) {
    Programs("برنامه‌ها"),
    Singers("خواننده‌ها"),
    Musicians("نوازندگان")
}

@Composable
fun LibraryScreen(
    initialTab: LibraryTab = LibraryTab.Programs,
    programs: List<ProgramUiModel> = emptyList(),
    singers: List<SingerListItemUiModel> = emptyList(),
    musicians: List<MusicianListItemUiModel> = emptyList(),
    isProgramsLoading: Boolean = false,
    isSingersLoading: Boolean = false,
    isMusiciansLoading: Boolean = false,
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    currentTrack: TrackUiModel? = null,
    isPlayerPlaying: Boolean = false,
    isPlayerLoading: Boolean = false,
    currentPlaybackPositionMs: Long = 0L,
    currentPlaybackDurationMs: Long = 0L,
    onTogglePlayerPlayback: () -> Unit = {},
    onProgramClick: (ProgramUiModel) -> Unit = {},
    onTrackClick: (Long) -> Unit = {},
) {
    val tabs = LibraryTab.entries
    val pagerState = rememberPagerState(
        initialPage = tabs.indexOf(initialTab).coerceAtLeast(0),
        pageCount = { tabs.size }
    )
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        com.radiogolha.mobile.theme.GolhaPatternBackground {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                bottomBar = {
                    BottomNavigationWithMiniPlayer(
                        items = bottomNavItems,
                        onItemSelected = onBottomNavSelected,
                        currentTrack = currentTrack,
                        isPlaying = isPlayerPlaying,
                        isLoading = isPlayerLoading,
                        currentPositionMs = currentPlaybackPositionMs,
                        durationMs = currentPlaybackDurationMs,
                        onTogglePlayback = onTogglePlayerPlayback,
                        onTrackClick = onTrackClick,
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                        .statusBarsPadding()
                ) {
                    // Header
                    Text(
                        text = "کتابخانه",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = GolhaColors.PrimaryText,
                        modifier = Modifier
                            .padding(horizontal = GolhaSpacing.ScreenHorizontal)
                            .padding(top = 16.dp, bottom = 12.dp)
                    )

                    // Tabs
                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = Color.Transparent,
                        contentColor = GolhaColors.PrimaryAccent,
                        edgePadding = GolhaSpacing.ScreenHorizontal,
                        divider = {},
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                color = GolhaColors.PrimaryAccent
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            val selected = pagerState.currentPage == index
                            Tab(
                                selected = selected,
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = {
                                    Text(
                                        text = tab.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 17.sp
                                        ),
                                        color = if (selected) GolhaColors.PrimaryText else GolhaColors.SecondaryText
                                    )
                                }
                            )
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(top = 8.dp)
                    ) { page ->
                        when (tabs[page]) {
                            LibraryTab.Programs -> {
                                ProgramsScreen(
                                    programs = programs,
                                    isLoading = isProgramsLoading,
                                    onProgramClick = onProgramClick
                                )
                            }

                            LibraryTab.Singers -> {
                                SingersContent(singers = singers)
                            }

                            LibraryTab.Musicians -> {
                                MusiciansContent(musicians = musicians)
                            }
                        }
                    }
                }
            }
        }
    }
}
