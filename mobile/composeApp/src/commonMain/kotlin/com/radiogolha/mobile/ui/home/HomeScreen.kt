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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaPatternBackground
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.programs.*

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
    onTrackLongClick: (TrackUiModel) -> Unit = {},
    orderedModes: List<String> = emptyList(),
    onDastgahClick: (String) -> Unit = {},
    onTrackClick: (TrackUiModel) -> Unit = {},
    onProgramClick: (ProgramUiModel) -> Unit = {},
    onSingerClick: (Long) -> Unit = {},
    onSingerLongPress: (SingerUiModel) -> Unit = {},
    onMusicianClick: (Long) -> Unit = {},
    onMusicianLongPress: (MusicianUiModel) -> Unit = {},
    duets: List<DuetPairUiModel> = emptyList(),
    onDuetClick: (DuetPairUiModel) -> Unit = {},
    savedPlaylists: List<SavedPlaylistUiModel> = emptyList(),
    onPlaylistClick: (Long) -> Unit = {},
    onExpandPlayer: () -> Unit = {},
    onBottomNavSelected: (AppTab) -> Unit = {},
    recentlyPlayed: List<TrackUiModel> = emptyList(),
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
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    if (state == null) {
                        item { HeaderSection() }
                        item { DuetsBannerSkeleton() }
                        item { ProgramsSectionSkeleton() }
                        item { SingersSectionSkeleton() }
                        item { DastgahSectionSkeleton() }
                        item { MusiciansSectionSkeleton() }
                        item { TopTracksSectionSkeleton() }
                    } else {
                        item { HeaderSection() }
                        item {
                            DuetsBanner(
                                duets = duets,
                                onDuetClick = onDuetClick,
                            )
                        }
                        item {
                            ProgramsSection(
                                programs = state.programs,
                                onProgramClick = onProgramClick
                            )
                        }
                        if (recentlyPlayed.isNotEmpty()) {
                            item {
                                RecentlyPlayedSection(
                                    tracks = recentlyPlayed,
                                    onTrackClick = onTrackClick,
                                    onPlayTrack = onPlayTrack,
                                    onTrackLongClick = onTrackLongClick,
                                    currentTrackId = currentTrack?.id,
                                    isPlayerPlaying = isPlayerPlaying,
                                )
                            }
                        }
                        item {
                            SingersSection(
                                singers = state.singers,
                                onSeeAllClick = onOpenAllSingers,
                                onSingerClick = onSingerClick,
                                onSingerLongPress = onSingerLongPress,
                            )
                        }
                        if (state.dastgahs.isNotEmpty()) {
                            item {
                                DastgahSection(items = state.dastgahs, orderedModes = orderedModes, onDastgahClick = onDastgahClick)
                            }
                        }
                        item {
                            MusiciansSection(
                                musicians = state.musicians,
                                onSeeAllClick = onOpenAllMusicians,
                                onMusicianClick = onMusicianClick,
                                onMusicianLongPress = onMusicianLongPress,
                            )
                        }
                        if (savedPlaylists.isNotEmpty()) {
                            item {
                                SavedPlaylistsSection(
                                    playlists = savedPlaylists,
                                    onPlaylistClick = onPlaylistClick,
                                )
                            }
                        }
                        item {
                            TopTracksSection(
                                tracks = state.topTracks,
                                isRefreshing = isRefreshingTopTracks,
                                onRefresh = onRefreshTopTracks,
                                onPlayTrack = onPlayTrack,
                                onTrackClick = onTrackClick,
                                onTrackLongClick = onTrackLongClick,
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
