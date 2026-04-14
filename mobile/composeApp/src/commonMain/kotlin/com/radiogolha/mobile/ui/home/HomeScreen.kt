package com.radiogolha.mobile.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
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
import com.radiogolha.mobile.theme.GolhaPatternBackground
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.programs.*

private val duetPairs = listOf(
    DuetPairUiModel("محمدرضا شجریان", "هایده"),
    DuetPairUiModel("غلامحسین بنان", "مرضیه"),
    DuetPairUiModel("مرضیه", "گلپا"),
    DuetPairUiModel("الهه", "محمدرضا شجریان"),
    DuetPairUiModel("هایده", "ایرج"),
)

@Composable
fun HomeScreen(
    state: HomeUiState?,
    bottomNavItems: List<BottomNavItemUiModel>,
    onOpenAllSingers: () -> Unit = {},
    onOpenAllMusicians: () -> Unit = {},
    isRefreshingTopTracks: Boolean = false,
    onRefreshTopTracks: () -> Unit = {},
    currentTrack: TrackUiModel? = null,
    isPlayerPlaying: Boolean = false,
    isPlayerLoading: Boolean = false,
    currentPlaybackPositionMs: Long = 0L,
    currentPlaybackDurationMs: Long = 0L,
    onTogglePlayerPlayback: () -> Unit = {},
    onPlayTrack: (TrackUiModel) -> Unit = {},
    onTrackClick: (TrackUiModel) -> Unit = {},
    onProgramClick: (ProgramUiModel) -> Unit = {},
    onSingerClick: (Long) -> Unit = {},
    onMusicianClick: (Long) -> Unit = {},
    onDuetClick: (DuetPairUiModel) -> Unit = {},
    onExpandPlayer: () -> Unit = {},
    onBottomNavSelected: (AppTab) -> Unit = {},
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        GolhaPatternBackground {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize(),
                containerColor = GolhaColors.ScreenBackground.copy(alpha = 0f),
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
                        onTrackClick = { trackId -> 
                            // Find the track in state if needed or just pass the ID
                            state?.topTracks?.find { it.id == trackId }?.let { onTrackClick(it) }
                                ?: currentTrack?.takeIf { it.id == trackId }?.let { onTrackClick(it) }
                        },
                        onExpand = onExpandPlayer,
                    )
                },
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    contentPadding = PaddingValues(
                        top = GolhaSpacing.StatusBarTopGap,
                        bottom = innerPadding.calculateBottomPadding() + 18.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(GolhaSpacing.SectionGap),
                ) {
                    if (state == null) {
                        item { HeaderSection() }
                        item { HeroBannerSkeleton() }
                        item { ProgramsSectionSkeleton() }
                        item { SingersSectionSkeleton() }
                        item { MusiciansSectionSkeleton() }
                        item { TopTracksSectionSkeleton() }
                    } else {
                        item { HeaderSection() }
                        item { HeroBanner() }
                        item { 
                            ProgramsSection(
                                programs = state.programs,
                                onProgramClick = onProgramClick
                            ) 
                        }
                        item {
                            SingersSection(
                                singers = state.singers,
                                onSeeAllClick = onOpenAllSingers,
                                onSingerClick = onSingerClick,
                            )
                        }
                        item {
                            MusiciansSection(
                                musicians = state.musicians,
                                onSeeAllClick = onOpenAllMusicians,
                                onMusicianClick = onMusicianClick,
                            )
                        }
                        item {
                            DuetsSection(
                                duets = duetPairs,
                                onDuetClick = onDuetClick,
                            )
                        }
                        item {
                            TopTracksSection(
                                tracks = state.topTracks,
                                isRefreshing = isRefreshingTopTracks,
                                onRefresh = onRefreshTopTracks,
                                onPlayTrack = onPlayTrack,
                                onTrackClick = onTrackClick,
                                onArtistClick = onSingerClick,
                                currentTrackId = currentTrack?.id,
                                isPlayerPlaying = isPlayerPlaying,
                            ) 
                        }
                    }
                }
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
            bottomNavItems = rememberSampleHomeUiState().bottomNavItems,
        )
    }
}
