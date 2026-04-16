package com.radiogolha.mobile.ui.home

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
import com.radiogolha.mobile.theme.GolhaSpacing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import com.radiogolha.mobile.ui.programs.ProgramTrackRow
import com.radiogolha.mobile.ui.programs.SkeletonTrackRow
import com.radiogolha.mobile.ui.programs.toTrackUiModel
import com.radiogolha.mobile.ui.root.TabRootScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DuetDetailScreen(
    singer1: String,
    singer2: String,
    singer1Avatar: String? = null,
    singer2Avatar: String? = null,
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    onBackClick: () -> Unit,
    onTrackClick: (Long) -> Unit = {},
    onPlayTrack: (TrackUiModel) -> Unit = {},
    onTrackLongClick: (TrackUiModel) -> Unit = {},
    currentTrack: TrackUiModel? = null,
    isPlayerPlaying: Boolean = false,
    isPlayerLoading: Boolean = false,
    currentPlaybackPositionMs: Long = 0L,
    currentPlaybackDurationMs: Long = 0L,
    onTogglePlayerPlayback: () -> Unit = {},
    onExpandPlayer: () -> Unit = {},
) {
    var programs by remember(singer1, singer2) { mutableStateOf<List<CategoryProgramUiModel>?>(null) }

    LaunchedEffect(singer1, singer2) {
        programs = withContext(Dispatchers.Default) {
            runCatching { loadDuetPrograms(singer1, singer2) }.getOrElse { emptyList() }
        }
    }

    val isLoading = programs == null

    TabRootScreen(
        title = "دوئت",
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
            // Duet banner
            item {
                DuetBannerCard(
                    singer1 = singer1, singer2 = singer2,
                    singer1Avatar = singer1Avatar, singer2Avatar = singer2Avatar,
                    trackCount = programs?.size ?: 0,
                )
            }

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
                                    onPlayClick = { onPlayTrack(program.toTrackUiModel()) },
                                    onLongClick = { onTrackLongClick(program.toTrackUiModel()) },
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
private fun DuetBannerCard(
    singer1: String, singer2: String,
    singer1Avatar: String?, singer2Avatar: String?,
    trackCount: Int,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.BannerBackground,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(140.dp)
                .drawBehind {
                    drawCircle(color = GolhaColors.BannerDetail.copy(alpha = 0.06f), radius = size.minDimension * 0.6f, center = Offset(size.width * 0.7f, size.height * 0.5f))
                    drawCircle(color = GolhaColors.BannerDetail.copy(alpha = 0.04f), radius = size.minDimension * 0.4f, center = Offset(size.width * 0.2f, size.height * 0.8f))
                },
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
                    Text("دوئت ماندگار", style = MaterialTheme.typography.labelSmall, color = GolhaColors.Surface.copy(alpha = 0.45f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("$singer1 و $singer2", style = MaterialTheme.typography.titleLarge, color = GolhaColors.BannerDetail, maxLines = 1)
                    if (trackCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$trackCount ترک", style = MaterialTheme.typography.labelSmall, color = GolhaColors.Surface.copy(alpha = 0.5f))
                    }
                }
                val inf = rememberInfiniteTransition(label = "duetRing")
                val borderRot by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(8000, easing = LinearEasing)), label = "rot")
                Box(modifier = Modifier.size(width = 130.dp, height = 90.dp)) {
                    Box(modifier = Modifier.size(80.dp).align(Alignment.CenterEnd)) {
                        AnimatedAvatarRing(rotation = borderRot, modifier = Modifier.fillMaxSize())
                        ArtistAvatar(name = singer2, imageUrl = singer2Avatar, tint = GolhaColors.BannerDetail, modifier = Modifier.fillMaxSize().padding(3.dp))
                    }
                    Box(modifier = Modifier.size(80.dp).align(Alignment.CenterStart)) {
                        AnimatedAvatarRing(rotation = -borderRot, modifier = Modifier.fillMaxSize())
                        ArtistAvatar(name = singer1, imageUrl = singer1Avatar, tint = GolhaColors.BannerDetail, modifier = Modifier.fillMaxSize().padding(3.dp))
                    }
                }
            }
        }
    }
}
