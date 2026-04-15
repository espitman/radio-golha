package com.radiogolha.mobile.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaRadius
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.*
import com.radiogolha.mobile.ui.programs.ProgramTrackRow
import com.radiogolha.mobile.ui.programs.SkeletonTrackRow
import com.radiogolha.mobile.ui.root.TabRootScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PlaylistDetailScreen(
    playlistName: String,
    filters: ActiveFilters,
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    onBackClick: () -> Unit,
    onProgramClick: (Long) -> Unit = {},
    onPlayTrack: (TrackUiModel) -> Unit = {},
    currentTrack: TrackUiModel? = null,
    isPlayerPlaying: Boolean = false,
    isPlayerLoading: Boolean = false,
    currentPlaybackPositionMs: Long = 0L,
    currentPlaybackDurationMs: Long = 0L,
    onTogglePlayerPlayback: () -> Unit = {},
    onTrackClick: (Long) -> Unit = {},
    onExpandPlayer: () -> Unit = {},
) {
    var results by remember { mutableStateOf<List<SearchResultUiModel>?>(null) }
    var allResults by remember { mutableStateOf<List<SearchResultUiModel>>(emptyList()) }
    var page by remember { mutableStateOf(1) }
    var totalPages by remember { mutableStateOf(1) }

    LaunchedEffect(filters) {
        val r = withContext(Dispatchers.Default) {
            runCatching { searchPrograms(filters, 1) }.getOrDefault(SearchResultsUiState())
        }
        results = r.results
        allResults = r.results
        page = r.page
        totalPages = r.totalPages
    }

    val isLoading = results == null

    TabRootScreen(
        title = playlistName,
        subtitle = "",
        bottomNavItems = bottomNavItems,
        onBottomNavSelected = onBottomNavSelected,
        currentTrack = currentTrack,
        isPlayerPlaying = isPlayerPlaying,
        isPlayerLoading = isPlayerLoading,
        currentPlaybackPositionMs = currentPlaybackPositionMs,
        currentPlaybackDurationMs = currentPlaybackDurationMs,
        onTogglePlayerPlayback = onTogglePlayerPlayback,
        onTrackClick = onTrackClick,
        onExpandPlayer = onExpandPlayer,
        onBackClick = onBackClick,
        content = {
            if (isLoading) {
                item {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .background(GolhaColors.Surface, RoundedCornerShape(GolhaRadius.Card))
                                .border(1.dp, GolhaColors.Border.copy(alpha = 0.6f), RoundedCornerShape(GolhaRadius.Card))
                                .padding(vertical = 8.dp),
                        ) {
                            repeat(8) { index ->
                                SkeletonTrackRow()
                                if (index != 7) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = GolhaColors.Border.copy(alpha = 0.65f))
                            }
                        }
                    }
                }
            } else if (allResults.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("برنامه‌ای یافت نشد", color = GolhaColors.SecondaryText)
                    }
                }
            } else {
                item {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .background(GolhaColors.Surface, RoundedCornerShape(GolhaRadius.Card))
                                .border(1.dp, GolhaColors.Border.copy(alpha = 0.6f), RoundedCornerShape(GolhaRadius.Card))
                                .padding(vertical = 8.dp),
                        ) {
                            allResults.forEachIndexed { index, result ->
                                val track = TrackUiModel(
                                    id = result.id,
                                    title = result.title,
                                    artist = result.artist ?: result.categoryName,
                                    duration = result.duration,
                                    audioUrl = result.audioUrl,
                                )
                                val isActive = currentTrack?.id == result.id
                                ProgramTrackRow(
                                    track = track,
                                    isActive = isActive,
                                    isPlaying = isActive && isPlayerPlaying,
                                    onTrackClick = { onProgramClick(result.id) },
                                    onPlayClick = { onPlayTrack(track) },
                                )
                                if (index != allResults.lastIndex) {
                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = GolhaColors.Border.copy(alpha = 0.65f))
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    )
}
