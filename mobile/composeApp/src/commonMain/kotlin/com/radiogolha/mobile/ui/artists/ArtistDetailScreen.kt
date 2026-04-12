package com.radiogolha.mobile.ui.artists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import radiogolha_mobile.composeapp.generated.resources.Res
import radiogolha_mobile.composeapp.generated.resources.eslimi_card_bg

private val ArtistHeaderCardHeight = 255.dp
private val ArtistTracksCardHeight = 418.dp
private const val ArtistSkeletonTrackCount = 5

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
                    item { ArtistHeaderLayout(isLoading = true) }
                    item { ArtistTracksSkeleton() }
                } else {
                    item { ArtistHeaderLayout(detail = resolvedDetail) }
                    item {
                        ArtistTracksCard(
                            tracks = resolvedDetail.tracks,
                            currentTrack = currentTrack,
                            isPlayerPlaying = isPlayerPlaying,
                            onProgramClick = onProgramClick,
                            onPlayTrack = onPlayTrack,
                            onArtistClick = onArtistClick,
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            },
        )
    }
}

@Composable
private fun ArtistHeaderLayout(
    detail: ArtistDetailUiModel? = null,
    isLoading: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.7f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ArtistHeaderCardHeight)
                .clip(RoundedCornerShape(GolhaRadius.Card))
        ) {
            // Background Pattern
            androidx.compose.foundation.Image(
                painter = painterResource(Res.drawable.eslimi_card_bg),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.32f
            )

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                GolhaColors.Surface.copy(alpha = 0.95f),
                                GolhaColors.Surface.copy(alpha = 0.4f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ArtistHeaderCardHeight)
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (isLoading || detail == null) {
                    // Skeleton State
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .background(GolhaColors.Border.copy(alpha = 0.2f), CircleShape)
                    )
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .height(40.dp)
                                .background(GolhaColors.Border.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        )
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(24.dp)
                                .background(GolhaColors.Border.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(84.dp)
                            .height(38.dp)
                            .background(GolhaColors.Border.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    )
                } else {
                    // Loaded State
                    ArtistAvatar(
                        name = detail.name,
                        imageUrl = detail.imageUrl,
                        tint = GolhaColors.SoftRose,
                        modifier = Modifier.size(104.dp),
                    )
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                        } else {
                            // Empty box to maintain height stability if no instrument
                            Box(modifier = Modifier.height(24.dp))
                        }
                    }

                    Surface(
                        color = GolhaColors.BadgeBackground.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, GolhaColors.Border)
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            text = "${detail.trackCount} برنامه",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = GolhaColors.PrimaryText,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistTracksCard(
    tracks: List<CategoryProgramUiModel>,
    currentTrack: TrackUiModel?,
    isPlayerPlaying: Boolean,
    onProgramClick: (CategoryProgramUiModel) -> Unit,
    onPlayTrack: (TrackUiModel) -> Unit,
    onArtistClick: (Long) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.65f)),
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ArtistTracksCardHeight)
                    .padding(vertical = 8.dp),
            ) {
                tracks.forEachIndexed { index, program ->
                    val isActive = currentTrack?.id == program.id
                    ProgramTrackRow(
                        track = program.toTrackUiModel(),
                        isActive = isActive,
                        isPlaying = isActive && isPlayerPlaying,
                        onTrackClick = { onProgramClick(program) },
                        onPlayClick = { onPlayTrack(program.toTrackUiModel()) },
                        onArtistClick = onArtistClick,
                    )
                    if (index != tracks.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            color = GolhaColors.Border.copy(alpha = 0.65f),
                        )
                    }
                }
                if (tracks.isEmpty()) {
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
                    .height(ArtistTracksCardHeight)
                    .padding(vertical = 8.dp),
            ) {
                repeat(ArtistSkeletonTrackCount) { index ->
                    SkeletonTrackRow()
                    if (index != ArtistSkeletonTrackCount - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            color = GolhaColors.Border.copy(alpha = 0.65f)
                        )
                    }
                }
            }
        }
    }
}
