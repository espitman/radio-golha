package com.radiogolha.mobile.ui.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaElevation
import com.radiogolha.mobile.theme.GolhaRadius
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.*
import com.radiogolha.mobile.ui.root.TabRootScreen

@Composable
fun CategoryProgramsScreen(
    categoryTitle: String,
    programs: List<CategoryProgramUiModel>,
    isLoading: Boolean = false,
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    onProgramClick: (CategoryProgramUiModel) -> Unit = {},
    onPlayTrack: (TrackUiModel) -> Unit,
    onArtistClick: (Long) -> Unit = {},
    onBackClick: () -> Unit,
    currentTrack: TrackUiModel? = null,
    isPlayerPlaying: Boolean = false,
    isPlayerLoading: Boolean = false,
    currentPlaybackPositionMs: Long = 0L,
    currentPlaybackDurationMs: Long = 0L,
    onTogglePlayerPlayback: () -> Unit = {},
    onTrackClick: (Long) -> Unit = {},
    onExpandPlayer: () -> Unit = {},
) {
    TabRootScreen(
        title = "برنامه",
        subtitle = categoryTitle,
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GolhaColors.Surface, RoundedCornerShape(GolhaRadius.Card))
                                .border(1.dp, GolhaColors.Border.copy(alpha = 0.6f), RoundedCornerShape(GolhaRadius.Card))
                                .padding(vertical = 8.dp),
                        ) {
                            repeat(8) { index ->
                                SkeletonTrackRow()
                                if (index != 7) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                        color = GolhaColors.Border.copy(alpha = 0.65f),
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (programs.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("برنامه‌ای یافت نشد", color = GolhaColors.SecondaryText)
                    }
                }
            } else {
                item {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GolhaColors.Surface, RoundedCornerShape(GolhaRadius.Card))
                                .border(1.dp, GolhaColors.Border.copy(alpha = 0.6f), RoundedCornerShape(GolhaRadius.Card))
                                .padding(vertical = 8.dp),
                        ) {
                            programs.forEachIndexed { index, program ->
                                val isActive = currentTrack?.id == program.id
                                ProgramTrackRow(
                                    track = program.toTrackUiModel(),
                                    isActive = isActive,
                                    isPlaying = isActive && isPlayerPlaying,
                                    onTrackClick = { onTrackClick(program.id) },
                                    onPlayClick = { onPlayTrack(program.toTrackUiModel()) },
                                    onArtistClick = onArtistClick,
                                )
                                if (index != programs.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                        color = GolhaColors.Border.copy(alpha = 0.65f),
                                    )
                                }
                            }
                        }
                    }
                }
                // Add spacer at bottom to ensure last item is above player
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    )
}
