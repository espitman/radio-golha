package com.radiogolha.mobile.ui.orchestras

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
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
    onPlayTrack: (TrackUiModel) -> Unit = {},
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
            // Banner
            item { OrchestraBanner(name = orchestraName, trackCount = programs?.size ?: 0) }

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
                            modifier = Modifier.fillMaxWidth()
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
                                    onPlayClick = { onPlayTrack(program.toTrackUiModel()) },
                                    onArtistClick = onArtistClick,
                                )
                                if (index != programs!!.lastIndex) {
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

@Composable
private fun OrchestraBanner(name: String, trackCount: Int) {
    val darkBg = Color(0xFF0B2161)
    val gold = Color(0xFFE3BF55)
    val textWhite = Color(0xFFF0ECE3)

    val inf = rememberInfiniteTransition(label = "orchBanner")
    val glowA by inf.animateFloat(0.04f, 0.10f, infiniteRepeatable(tween(3500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "a")
    val glowR by inf.animateFloat(0.40f, 0.55f, infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "r")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = darkBg,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(100.dp)
                .drawBehind {
                    drawCircle(gold.copy(alpha = glowA), size.minDimension * glowR, Offset(size.width * 0.8f, size.height * 0.3f))
                    drawCircle(gold.copy(alpha = glowA * 0.6f), size.minDimension * glowR * 0.7f, Offset(size.width * 0.15f, size.height * 0.75f))
                }
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = gold)
                if (trackCount > 0) {
                    Text("$trackCount برنامه", style = MaterialTheme.typography.labelSmall, color = textWhite.copy(alpha = 0.5f))
                }
            }
        }
    }
}
