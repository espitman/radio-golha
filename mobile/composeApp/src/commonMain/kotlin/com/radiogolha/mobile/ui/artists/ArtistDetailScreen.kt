package com.radiogolha.mobile.ui.artists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaRadius
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.*
import com.radiogolha.mobile.ui.programs.*
import com.radiogolha.mobile.ui.root.TabRootScreen

@Composable
fun ArtistDetailScreen(
    artistId: Long,
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    onBackClick: () -> Unit,
    onProgramClick: (CategoryProgramUiModel) -> Unit = {},
    onPlayTrack: (TrackUiModel) -> Unit = {},
    onArtistClick: (Long) -> Unit = {},
    currentTrack: TrackUiModel? = null,
    isPlayerPlaying: Boolean = false,
    isPlayerLoading: Boolean = false,
    currentPlaybackPositionMs: Long = 0L,
    currentPlaybackDurationMs: Long = 0L,
    onTogglePlayerPlayback: () -> Unit = {},
    onTrackClick: (Long) -> Unit = {},
) {
    val detail by produceState<ArtistDetailUiModel?>(initialValue = null, key1 = artistId) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            loadArtistDetail(artistId)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        TabRootScreen(
            title = "هنرمند",
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
            onBackClick = onBackClick,
            content = {
                val resolvedDetail = detail
                if (resolvedDetail == null) {
                    item { ArtistHeaderSkeleton() }
                    item { ArtistTracksSkeleton() }
                } else {
                    item { ArtistHeaderCard(detail = resolvedDetail) }
                    item {
                        Surface(
                            shape = RoundedCornerShape(GolhaRadius.Card),
                            color = GolhaColors.Surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.65f)),
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                ) {
                                    resolvedDetail.tracks.forEachIndexed { index, program ->
                                        val isActive = currentTrack?.id == program.id
                                        ProgramTrackRow(
                                            track = program.toTrackUiModel(),
                                            isActive = isActive,
                                            isPlaying = isActive && isPlayerPlaying,
                                            onTrackClick = { onProgramClick(program) },
                                            onPlayClick = { onPlayTrack(program.toTrackUiModel()) },
                                            onArtistClick = onArtistClick,
                                        )
                                        if (index != resolvedDetail.tracks.lastIndex) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                                color = GolhaColors.Border.copy(alpha = 0.65f),
                                            )
                                        }
                                    }
                                    if (resolvedDetail.tracks.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 36.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = "ترکی برای این هنرمند پیدا نشد",
                                                color = GolhaColors.SecondaryText,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            },
        )
    }
}

@Composable
private fun ArtistHeaderCard(detail: ArtistDetailUiModel) {
    Surface(
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.65f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ArtistAvatar(
                name = detail.name,
                imageUrl = detail.imageUrl,
                tint = GolhaColors.SoftBlue,
                modifier = Modifier.size(104.dp),
            )
            Text(
                text = detail.name,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = GolhaColors.PrimaryText,
                textAlign = TextAlign.Center,
            )
            if (!detail.instrument.isNullOrBlank()) {
                Text(
                    text = detail.instrument,
                    style = MaterialTheme.typography.titleMedium,
                    color = GolhaColors.SecondaryText,
                    textAlign = TextAlign.Center,
                )
            }
            Text(
                text = "${detail.trackCount} ترک",
                style = MaterialTheme.typography.bodyMedium,
                color = GolhaColors.SecondaryText,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ArtistHeaderSkeleton() {
    Surface(
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.65f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .background(GolhaColors.Border.copy(alpha = 0.24f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(18.dp)
                    .background(GolhaColors.Border.copy(alpha = 0.24f), RoundedCornerShape(10.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(14.dp)
                    .background(GolhaColors.Border.copy(alpha = 0.20f), RoundedCornerShape(10.dp))
            )
        }
    }
}

@Composable
private fun ArtistTracksSkeleton() {
    Surface(
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.65f)),
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                repeat(6) { index ->
                    SkeletonTrackRow()
                    if (index != 5) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            color = GolhaColors.Border.copy(alpha = 0.65f),
                        )
                    }
                }
            }
        }
    }
}
