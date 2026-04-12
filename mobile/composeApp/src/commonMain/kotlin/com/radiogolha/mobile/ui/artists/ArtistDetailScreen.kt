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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaRadius
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.AppTab
import com.radiogolha.mobile.ui.home.ArtistAvatar
import com.radiogolha.mobile.ui.home.ArtistDetailUiModel
import com.radiogolha.mobile.ui.home.BottomNavItemUiModel
import com.radiogolha.mobile.ui.home.CategoryProgramUiModel
import com.radiogolha.mobile.ui.home.GolhaIcon
import com.radiogolha.mobile.ui.home.GolhaLineIcon
import com.radiogolha.mobile.ui.home.TrackUiModel
import com.radiogolha.mobile.ui.root.TabRootScreen

@Composable
fun ArtistDetailScreen(
    artistId: Long,
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    onBackClick: () -> Unit,
    onProgramClick: (CategoryProgramUiModel) -> Unit,
    onPlayTrack: (TrackUiModel) -> Unit,
    currentTrack: TrackUiModel? = null,
    isPlayerPlaying: Boolean = false,
    isPlayerLoading: Boolean = false,
    currentPlaybackPositionMs: Long = 0L,
    currentPlaybackDurationMs: Long = 0L,
    onTogglePlayerPlayback: () -> Unit = {},
    onTrackClick: (Long) -> Unit = {},
) {
    val detail by produceState<ArtistDetailUiModel?>(initialValue = null, key1 = artistId) {
        value = loadArtistDetail(artistId)
    }

    TabRootScreen(
        title = detail?.name ?: "در حال بارگذاری...",
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
                    ArtistTracksCard(
                        title = resolvedDetail.name,
                        tracks = resolvedDetail.tracks,
                        currentTrack = currentTrack,
                        isPlayerPlaying = isPlayerPlaying,
                        onProgramClick = onProgramClick,
                        onPlayTrack = onPlayTrack,
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        },
    )
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
private fun ArtistTracksCard(
    title: String,
    tracks: List<CategoryProgramUiModel>,
    currentTrack: TrackUiModel?,
    isPlayerPlaying: Boolean,
    onProgramClick: (CategoryProgramUiModel) -> Unit,
    onPlayTrack: (TrackUiModel) -> Unit,
) {
    if (tracks.isEmpty()) {
        Surface(
            shape = RoundedCornerShape(GolhaRadius.Card),
            color = GolhaColors.Surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.65f)),
        ) {
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
        return
    }

    Surface(
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.65f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            tracks.forEachIndexed { index, program ->
                val isActive = currentTrack?.id == program.id
                ArtistTrackRow(
                    title = title,
                    program = program,
                    isActive = isActive,
                    isPlaying = isActive && isPlayerPlaying,
                    onRowClick = { onProgramClick(program) },
                    onPlayClick = {
                        onPlayTrack(
                            TrackUiModel(
                                id = program.id,
                                title = "$title ${program.programNumber}",
                                artist = program.singer,
                                duration = program.duration,
                                audioUrl = program.audioUrl,
                            )
                        )
                    },
                )
                if (index != tracks.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = GolhaColors.Border.copy(alpha = 0.65f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistTrackRow(
    title: String,
    program: CategoryProgramUiModel,
    isActive: Boolean,
    isPlaying: Boolean,
    onRowClick: () -> Unit,
    onPlayClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClick() }
            .background(if (isActive) GolhaColors.BadgeBackground.copy(alpha = 0.42f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(GolhaColors.SoftSand, RoundedCornerShape(14.dp))
                    .border(1.dp, GolhaColors.Border.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = program.programNumber,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = GolhaColors.PrimaryAccent,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "$title ${program.programNumber}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = GolhaColors.PrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitle = program.dastgah ?: program.singer
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = GolhaColors.SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (!program.duration.isNullOrBlank()) {
            Text(
                text = program.duration.orEmpty(),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                color = GolhaColors.SecondaryText,
            )
        }

        Surface(
            modifier = Modifier
                .size(38.dp)
                .clickable { onPlayClick() },
            shape = CircleShape,
            color = if (isActive) GolhaColors.PrimaryAccent else GolhaColors.BadgeBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
        ) {
            Box(contentAlignment = Alignment.Center) {
                GolhaLineIcon(
                    icon = if (isActive && isPlaying) GolhaIcon.Pause else GolhaIcon.Play,
                    modifier = Modifier.size(18.dp),
                    tint = if (isActive) Color.White else GolhaColors.PrimaryText,
                )
            }
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            repeat(6) { index ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(GolhaColors.Border.copy(alpha = 0.24f), RoundedCornerShape(14.dp))
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.58f)
                                .height(14.dp)
                                .background(GolhaColors.Border.copy(alpha = 0.24f), RoundedCornerShape(10.dp))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.36f)
                                .height(12.dp)
                                .background(GolhaColors.Border.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(GolhaColors.Border.copy(alpha = 0.24f), CircleShape)
                    )
                }
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
