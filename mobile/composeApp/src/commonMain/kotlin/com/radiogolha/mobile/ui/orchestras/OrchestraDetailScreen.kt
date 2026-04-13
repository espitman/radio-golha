package com.radiogolha.mobile.ui.orchestras

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.radiogolha.mobile.ui.home.*
import com.radiogolha.mobile.ui.programs.ProgramTrackRow
import com.radiogolha.mobile.ui.programs.SkeletonTrackRow
import com.radiogolha.mobile.ui.programs.toTrackUiModel
import com.radiogolha.mobile.ui.root.TabRootScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun OrchestraDetailScreen(
    orchestraId: Long,
    orchestraName: String,
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    onBackClick: () -> Unit,
    onTrackClick: (Long) -> Unit = {},
    onArtistClick: (Long) -> Unit = {},
    currentTrack: TrackUiModel? = null,
    isPlayerPlaying: Boolean = false,
    isPlayerLoading: Boolean = false,
    currentPlaybackPositionMs: Long = 0L,
    currentPlaybackDurationMs: Long = 0L,
    onTogglePlayerPlayback: () -> Unit = {},
    onExpandPlayer: () -> Unit = {},
) {
    var programs by remember(orchestraId) { mutableStateOf<List<CategoryProgramUiModel>?>(null) }

    LaunchedEffect(orchestraId) {
        programs = withContext(Dispatchers.Default) {
            runCatching { loadProgramsByOrchestra(orchestraId) }.getOrElse { emptyList() }
        }
    }

    val isLoading = programs == null

    TabRootScreen(
        title = "ارکستر",
        subtitle = orchestraName,
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
            } else if (programs.isNullOrEmpty()) {
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
                            programs!!.forEachIndexed { index, program ->
                                val isActive = currentTrack?.id == program.id
                                ProgramTrackRow(
                                    track = program.toTrackUiModel(),
                                    isActive = isActive,
                                    isPlaying = isActive && isPlayerPlaying,
                                    onTrackClick = { onTrackClick(program.id) },
                                    onPlayClick = { onTrackClick(program.id) },
                                    onArtistClick = onArtistClick,
                                )
                                if (index != programs!!.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                        color = GolhaColors.Border.copy(alpha = 0.65f),
                                    )
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
