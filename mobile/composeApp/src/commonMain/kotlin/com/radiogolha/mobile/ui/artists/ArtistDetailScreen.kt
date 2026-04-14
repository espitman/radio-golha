package com.radiogolha.mobile.ui.artists

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaRadius
import com.radiogolha.mobile.ui.home.*
import com.radiogolha.mobile.ui.programs.*
import com.radiogolha.mobile.ui.root.TabRootScreen
import org.jetbrains.compose.resources.painterResource
import radiogolha_mobile.composeapp.generated.resources.Res
import radiogolha_mobile.composeapp.generated.resources.eslimi_card_bg

private val artistDetailCache = mutableMapOf<Long, ArtistDetailUiModel?>()

@OptIn(ExperimentalFoundationApi::class)
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
    onExpandPlayer: () -> Unit = {},
) {
    var detail by remember(artistId) { mutableStateOf(artistDetailCache[artistId]) }
    val scrollState = rememberSaveable(artistId, saver = LazyListState.Saver) {
        LazyListState()
    }

    LaunchedEffect(artistId) {
        if (detail == null) {
            detail = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                loadArtistDetail(artistId)
            }
            artistDetailCache[artistId] = detail
        }
    }
    
    // Calculate collapse progress based on scroll
    // The threshold is the height of the title section + some padding
    val headerThreshold = with(LocalDensity.current) { 100.dp.toPx() }
    val collapseProgress by remember {
        derivedStateOf {
            if (scrollState.firstVisibleItemIndex > 0) 1f
            else (scrollState.firstVisibleItemScrollOffset / headerThreshold).coerceIn(0f, 1f)
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
            onExpandPlayer = onExpandPlayer,
            onBackClick = onBackClick,
            scrollState = scrollState,
            content = {
                val resolvedDetail = detail
                
                // Sticky Shrinking Header
                stickyHeader {
                    ArtistHeaderLayout(
                        detail = resolvedDetail,
                        isLoading = resolvedDetail == null,
                        progress = collapseProgress
                    )
                }

                if (resolvedDetail == null) {
                    item { ArtistTracksSkeleton() }
                } else {
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
private fun ArtistHeaderLayout(
    detail: ArtistDetailUiModel? = null,
    isLoading: Boolean = false,
    progress: Float = 0f // 0 = Expand, 1 = Collapse
) {
    // Animate values based on progress
    val currentPadding = lerp(18.dp, 10.dp, progress)
    val avatarSize = lerp(104.dp, 44.dp, progress)
    val titleFontSize = lerp(24.sp, 18.sp, progress)
    val contentAlpha = 1f - progress
    val isSticky = progress > 0.95f

    val darkBg = Color(0xFF0B2161)
    val gold = Color(0xFFE3BF55)
    val textWhite = Color(0xFFF0ECE3)
    val textDim = Color(0xFF8A95A8)
    val skeletonColor = Color.White.copy(alpha = 0.1f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isSticky) 0.dp else 4.dp),
        shape = if (isSticky) RoundedCornerShape(0.dp) else RoundedCornerShape(GolhaRadius.Card),
        color = darkBg,
        shadowElevation = if (isSticky) 4.dp else 0.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth().drawBehind {
            drawCircle(gold.copy(alpha = 0.06f), size.minDimension * 0.5f, Offset(size.width * 0.8f, size.height * 0.3f))
            drawCircle(gold.copy(alpha = 0.04f), size.minDimension * 0.35f, Offset(size.width * 0.15f, size.height * 0.8f))
        }) {
            if (progress < 0.8f) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(currentPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ArtistAvatarBox(detail, isLoading, avatarSize)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.alpha(if (isLoading) 1f else contentAlpha),
                    ) {
                        if (isLoading || detail == null) {
                            Box(Modifier.width(160.dp).height(32.dp).background(skeletonColor, RoundedCornerShape(4.dp)))
                            Box(Modifier.width(100.dp).height(24.dp).background(skeletonColor, RoundedCornerShape(4.dp)))
                        } else {
                            Text(detail.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = titleFontSize), color = textWhite, textAlign = TextAlign.Center)
                            if (!detail.instrument.isNullOrBlank()) {
                                Text(detail.instrument, style = MaterialTheme.typography.titleMedium, color = gold, textAlign = TextAlign.Center)
                            } else {
                                Box(modifier = Modifier.height(24.dp))
                            }
                        }
                    }

                    if (!isLoading && detail != null) {
                        Surface(
                            modifier = Modifier.alpha(contentAlpha),
                            color = gold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                text = "${detail.trackCount} برنامه",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                color = gold,
                            )
                        }
                    } else {
                        Box(Modifier.width(84.dp).height(38.dp).background(skeletonColor, RoundedCornerShape(10.dp)))
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ArtistAvatarBox(detail, isLoading, 40.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        if (isLoading || detail == null) {
                            Box(Modifier.width(120.dp).height(20.dp).background(skeletonColor, RoundedCornerShape(4.dp)))
                        } else {
                            Text(detail.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = textWhite, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        if (!isLoading && detail != null && !detail.instrument.isNullOrBlank()) {
                            Text(detail.instrument, style = MaterialTheme.typography.bodySmall, color = gold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        } else if (isLoading) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(Modifier.width(80.dp).height(14.dp).background(skeletonColor, RoundedCornerShape(4.dp)))
                        }
                    }
                    if (!isLoading && detail != null) {
                        Text("${detail.trackCount} برنامه", style = MaterialTheme.typography.labelSmall, color = textDim)
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistAvatarBox(detail: ArtistDetailUiModel?, isLoading: Boolean, size: Dp) {
    if (isLoading || detail == null) {
        Box(
            modifier = Modifier
                .size(size)
                .background(GolhaColors.Border.copy(alpha = 0.2f), CircleShape)
        )
    } else {
        ArtistAvatar(
            name = detail.name,
            imageUrl = detail.imageUrl,
            tint = GolhaColors.SoftRose,
            modifier = Modifier.size(size),
        )
    }
}

private fun lerp(start: Dp, end: Dp, fraction: Float): Dp {
    return start + (end - start) * fraction
}

private fun lerp(start: androidx.compose.ui.unit.TextUnit, end: androidx.compose.ui.unit.TextUnit, fraction: Float): androidx.compose.ui.unit.TextUnit {
    return (start.value + (end.value - start.value) * fraction).sp
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(GolhaColors.Border.copy(alpha = 0.25f))
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(GolhaColors.Border.copy(alpha = 0.25f))
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.4f)
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(GolhaColors.Border.copy(alpha = 0.15f))
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(11.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(GolhaColors.Border.copy(alpha = 0.18f))
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GolhaColors.Border.copy(alpha = 0.2f))
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
}
