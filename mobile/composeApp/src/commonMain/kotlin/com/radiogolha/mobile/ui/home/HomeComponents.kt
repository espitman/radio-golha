package com.radiogolha.mobile.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaElevation
import com.radiogolha.mobile.theme.GolhaRadius
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.theme.GolhaTypographyTokens
import com.radiogolha.mobile.ui.programs.*
import kotlin.math.min

@Composable
fun SectionTitle(
    title: String,
    onSeeAllClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = GolhaSpacing.ScreenHorizontal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = GolhaColors.PrimaryText,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = GolhaColors.Border,
            thickness = 1.dp,
        )
        if (onSeeAllClick != null) {
            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(GolhaRadius.Small))
                    .clickable { onSeeAllClick() }
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                text = "همه",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = GolhaColors.SecondaryText,
            )
        }
    }
}

@Composable
fun HeaderSection(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = GolhaSpacing.ScreenHorizontal),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "گل‌ها",
            style = MaterialTheme.typography.headlineLarge,
            color = GolhaColors.PrimaryText,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(GolhaSpacing.Small),
        ) {
            CircularActionButton(icon = GolhaIcon.Favorites)
            CircularActionButton(icon = GolhaIcon.Profile)
        }
    }
}

// Premium hero banner with a restrained Persian visual accent.
@Composable
fun HeroBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .requiredHeight(220.dp),
        shape = RoundedCornerShape(0.dp),
        color = GolhaColors.BannerBackground,
        tonalElevation = GolhaElevation.Banner,
        shadowElevation = GolhaElevation.Banner,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(GolhaColors.BannerBackground)

                    drawCircle(
                        color = GolhaColors.BannerDetail.copy(alpha = 0.08f),
                        radius = size.minDimension * 0.42f,
                        center = Offset(size.width * 0.18f, size.height * 0.20f),
                    )
                    drawCircle(
                        color = GolhaColors.BannerDetail.copy(alpha = 0.07f),
                        radius = size.minDimension * 0.33f,
                        center = Offset(size.width * 0.88f, size.height * 0.84f),
                    )

                    val swirl = Path().apply {
                        moveTo(size.width * 0.12f, size.height * 0.22f)
                        cubicTo(
                            size.width * 0.28f,
                            size.height * 0.04f,
                            size.width * 0.52f,
                            size.height * 0.18f,
                            size.width * 0.62f,
                            size.height * 0.08f,
                        )
                        cubicTo(
                            size.width * 0.74f,
                            size.height * 0.00f,
                            size.width * 0.78f,
                            size.height * 0.16f,
                            size.width * 0.92f,
                            size.height * 0.14f,
                        )
                    }
                    drawPath(
                        path = swirl,
                        color = GolhaColors.BannerDetail.copy(alpha = 0.12f),
                        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round),
                    )

                    repeat(5) { index ->
                        val x = size.width * (0.18f + index * 0.12f)
                        drawCircle(
                            color = GolhaColors.BannerDetail.copy(alpha = 0.09f),
                            radius = 4.dp.toPx(),
                            center = Offset(x, size.height * 0.76f),
                        )
                    }
                }
                .padding(horizontal = 22.dp, vertical = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "گل‌های رنگارنگ",
                        style = MaterialTheme.typography.displaySmall,
                        color = GolhaColors.BannerDetail,
                    )
                    Text(
                        text = "برنامه‌ای خاطره‌انگیز",
                        modifier = Modifier.padding(top = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = GolhaColors.Surface.copy(alpha = 0.82f),
                    )

                    Row(
                        modifier = Modifier.padding(top = 22.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PlayPillButton()
                    }
                }
                VintageRadioIllustration()
            }
        }
    }
}

// Programs are intentionally simple white cards with no iconography.
@Composable
fun ProgramsSection(
    programs: List<ProgramUiModel>,
    onProgramClick: (ProgramUiModel) -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(GolhaSpacing.Large)) {
        SectionTitle(title = "برنامه‌ها")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GolhaSpacing.CardGap),
            contentPadding = PaddingValues(horizontal = GolhaSpacing.ScreenHorizontal),
        ) {
            items(programs) { program ->
                ProgramCard(
                    item = program,
                    modifier = Modifier.widthIn(min = 128.dp, max = 164.dp),
                    onClick = { onProgramClick(program) }
                )
            }
        }
    }
}

@Composable
fun SingersSection(
    singers: List<SingerUiModel>,
    onSeeAllClick: () -> Unit,
    onSingerClick: (Long) -> Unit = {},
) {
    Column(verticalArrangement = Arrangement.spacedBy(GolhaSpacing.Large)) {
        SectionTitle(
            title = "خواننده‌ها",
            onSeeAllClick = onSeeAllClick,
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(horizontal = GolhaSpacing.ScreenHorizontal),
        ) {
            items(singers) { singer ->
                AvatarNameItem(
                    title = singer.name,
                    subtitle = null,
                    imageUrl = singer.imageUrl,
                    tint = GolhaColors.SoftBlue,
                    onClick = { onSingerClick(singer.id) },
                )
            }
        }
    }
}

@Composable
fun DastgahSection(items: List<DastgahUiModel>) {
    Column(verticalArrangement = Arrangement.spacedBy(GolhaSpacing.Large)) {
        SectionTitle(title = "دستگاه‌ها")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = GolhaSpacing.ScreenHorizontal),
        ) {
            items(items) { item ->
                DastgahChip(name = item.name)
            }
        }
    }
}

@Composable
fun MusiciansSection(
    musicians: List<MusicianUiModel>,
    onSeeAllClick: () -> Unit,
    onMusicianClick: (Long) -> Unit = {},
) {
    Column(verticalArrangement = Arrangement.spacedBy(GolhaSpacing.Large)) {
        SectionTitle(
            title = "نوازندگان برجسته",
            onSeeAllClick = onSeeAllClick,
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(horizontal = GolhaSpacing.ScreenHorizontal),
        ) {
            items(musicians) { musician ->
                AvatarNameItem(
                    title = musician.name,
                    subtitle = musician.instrument,
                    imageUrl = musician.imageUrl,
                    tint = GolhaColors.SoftRose,
                    onClick = { onMusicianClick(musician.id) },
                )
            }
        }
    }
}

@Composable
fun DuetsBanner(
    duets: List<DuetPairUiModel>,
    onDuetClick: (DuetPairUiModel) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { duets.size })

    // Animated glow circles
    val infiniteTransition = rememberInfiniteTransition(label = "duetGlow")
    val glowRadius1 by infiniteTransition.animateFloat(
        initialValue = 0.55f, targetValue = 0.68f,
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "glow1",
    )
    val glowRadius2 by infiniteTransition.animateFloat(
        initialValue = 0.35f, targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "glow2",
    )
    val glowAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.04f, targetValue = 0.09f,
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "glowA1",
    )
    val glowAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.03f, targetValue = 0.07f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "glowA2",
    )
    // Border rotation for avatar rings
    val borderRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "borderRot",
    )

    Box(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            val duet = duets[page]
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(180.dp)
                    .clickable { onDuetClick(duet) },
                shape = RoundedCornerShape(0.dp),
                color = GolhaColors.BannerBackground,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawCircle(
                                color = GolhaColors.BannerDetail.copy(alpha = glowAlpha1),
                                radius = size.minDimension * glowRadius1,
                                center = Offset(size.width * 0.7f, size.height * 0.5f),
                            )
                            drawCircle(
                                color = GolhaColors.BannerDetail.copy(alpha = glowAlpha2),
                                radius = size.minDimension * glowRadius2,
                                center = Offset(size.width * 0.2f, size.height * 0.8f),
                            )
                        },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Text (right in RTL)
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "دوئت ماندگار",
                                style = MaterialTheme.typography.labelSmall,
                                color = GolhaColors.Surface.copy(alpha = 0.45f),
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${duet.singer1} و ${duet.singer2}",
                                style = MaterialTheme.typography.titleLarge,
                                color = GolhaColors.BannerDetail,
                                maxLines = 1,
                            )
                            if (duet.trackCount > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${duet.trackCount} ترک",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GolhaColors.Surface.copy(alpha = 0.5f),
                                )
                            }
                        }

                        // Avatars with animated border
                        Box(modifier = Modifier.size(width = 150.dp, height = 100.dp)) {
                            Box(modifier = Modifier.size(90.dp).align(Alignment.CenterEnd)) {
                                AnimatedAvatarRing(rotation = borderRotation, modifier = Modifier.fillMaxSize())
                                ArtistAvatar(
                                    name = duet.singer2, imageUrl = duet.singer2Avatar, tint = GolhaColors.BannerDetail,
                                    modifier = Modifier.fillMaxSize().padding(3.dp),
                                )
                            }
                            Box(modifier = Modifier.size(90.dp).align(Alignment.CenterStart)) {
                                AnimatedAvatarRing(rotation = -borderRotation, modifier = Modifier.fillMaxSize())
                                ArtistAvatar(
                                    name = duet.singer1, imageUrl = duet.singer1Avatar, tint = GolhaColors.BannerDetail,
                                    modifier = Modifier.fillMaxSize().padding(3.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Page indicator dots
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(duets.size) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (selected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (selected) GolhaColors.BannerDetail else GolhaColors.Surface.copy(alpha = 0.3f)),
                )
            }
        }
    }
}

@Composable
private fun AnimatedAvatarRing(rotation: Float, modifier: Modifier = Modifier) {
    val gold = GolhaColors.BannerDetail
    Canvas(modifier = modifier.graphicsLayer { rotationZ = rotation }) {
        val stroke = 2.5.dp.toPx()
        val radius = (size.minDimension - stroke) / 2f
        // Full ring
        drawCircle(color = gold.copy(alpha = 0.3f), radius = radius, style = Stroke(width = stroke))
        // Bright arc segment
        drawArc(
            color = gold.copy(alpha = 0.9f),
            startAngle = 0f, sweepAngle = 90f, useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
            topLeft = Offset(stroke / 2f, stroke / 2f),
            size = Size(size.width - stroke, size.height - stroke),
        )
    }
}

@Composable
fun DuetsBannerSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .requiredHeight(140.dp)
            .padding(horizontal = GolhaSpacing.ScreenHorizontal)
            .clip(RoundedCornerShape(GolhaRadius.Card))
            .background(GolhaColors.Border.copy(alpha = 0.2f)),
    )
}

@Composable
fun TopTracksSection(
    tracks: List<TrackUiModel>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onPlayTrack: (TrackUiModel) -> Unit,
    onArtistClick: (Long) -> Unit = {},
    onTrackClick: (TrackUiModel) -> Unit = {},
    currentTrackId: Long? = null,
    isPlayerPlaying: Boolean = false,
    modifier: Modifier = Modifier
) {
    val trackRowCount = 5
    val displayedTracks = remember(tracks) {
        tracks.take(trackRowCount)
    }
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            )
            rotation.snapTo(0f)
        } else {
            rotation.snapTo(0f)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(GolhaSpacing.Large)
    ) {
        // Section title + refresh button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GolhaSpacing.ScreenHorizontal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "ترک‌های برتر",
                style = MaterialTheme.typography.titleLarge,
                color = GolhaColors.PrimaryText,
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = GolhaColors.Border.copy(alpha = 0.6f),
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(GolhaColors.BadgeBackground)
                    .border(1.dp, GolhaColors.Border, CircleShape)
                    .clickable(enabled = !isRefreshing) { onRefresh() },
                contentAlignment = Alignment.Center
            ) {
                GolhaLineIcon(
                    icon = GolhaIcon.Refresh,
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer { rotationZ = rotation.value },
                    tint = GolhaColors.SecondaryText,
                )
            }
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            if (isRefreshing && tracks.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = GolhaSpacing.ScreenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(trackRowCount) { index ->
                        SkeletonTrackRow()
                        if (index != trackRowCount - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = GolhaColors.Border.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = GolhaSpacing.ScreenHorizontal),
                ) {
                    displayedTracks.forEachIndexed { index, track ->
                        ProgramTrackRow(
                            track = track,
                            isActive = track.id == currentTrackId,
                            isPlaying = track.id == currentTrackId && isPlayerPlaying,
                            onPlayClick = { onPlayTrack(track) },
                            onTrackClick = { onTrackClick(track) },
                            onArtistClick = onArtistClick,
                        )
                        if (index != displayedTracks.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = GolhaColors.Border.copy(alpha = 0.65f),
                            )
                        }
                    }
                    if (displayedTracks.isEmpty()) {
                        TopTracksEmptyState()
                    }
                }
            }
        }
    }
}

@Composable
private fun TopTracksEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "ترکی برای نمایش پیدا نشد",
            style = MaterialTheme.typography.bodyMedium,
            color = GolhaColors.SecondaryText,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun BottomNavigationWithMiniPlayer(
    items: List<BottomNavItemUiModel>,
    onItemSelected: (AppTab) -> Unit,
    currentTrack: TrackUiModel?,
    isPlaying: Boolean,
    isLoading: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onTogglePlayback: () -> Unit,
    onTrackClick: (Long) -> Unit = {},
    onExpand: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        MiniPlayerBar(
            currentTrack = currentTrack,
            isPlaying = isPlaying,
            isLoading = isLoading,
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            onTogglePlayback = onTogglePlayback,
            onTrackClick = onTrackClick,
            onExpand = onExpand,
        )
        BottomNavigationBar(
            items = items,
            onItemSelected = onItemSelected,
        )
    }
}

@Composable
fun MiniPlayerBar(
    currentTrack: TrackUiModel?,
    isPlaying: Boolean,
    isLoading: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onTogglePlayback: () -> Unit,
    onTrackClick: (Long) -> Unit = {},
    onExpand: () -> Unit = {},
) {
    Surface(
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = GolhaColors.Surface.copy(alpha = 0.98f),
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.82f)),
        modifier = Modifier
            .clickable(enabled = currentTrack != null) { onExpand() }
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    if (dragAmount < -15) {
                        onExpand()
                        change.consume()
                    }
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GolhaSpacing.ScreenHorizontal, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val playbackProgress = remember(currentTrack?.id, currentPositionMs, durationMs) {
                if (durationMs > 0L) {
                    (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
            }
            Box(
                modifier = Modifier
                    .size(46.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    progress = { playbackProgress },
                    modifier = Modifier.matchParentSize(),
                    color = GolhaColors.PrimaryText,
                    trackColor = GolhaColors.Border.copy(alpha = 0.55f),
                    strokeWidth = 2.6.dp,
                )

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(GolhaColors.BadgeBackground)
                        .border(1.dp, GolhaColors.Border, CircleShape)
                        .clickable(enabled = !currentTrack?.audioUrl.isNullOrBlank()) { onTogglePlayback() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = GolhaColors.PrimaryText,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        GolhaLineIcon(
                            icon = if (isPlaying) GolhaIcon.Pause else GolhaIcon.Play,
                            modifier = Modifier.size(18.dp),
                            tint = if (!currentTrack?.audioUrl.isNullOrBlank()) GolhaColors.PrimaryText else GolhaColors.SecondaryText.copy(alpha = 0.55f),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = currentTrack?.title ?: "...",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = GolhaColors.PrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = currentTrack?.artist ?: "...",
                    style = MaterialTheme.typography.bodySmall,
                    color = GolhaColors.SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            val formattedPlaybackTime = remember(currentTrack?.id, currentPositionMs, durationMs) {
                formatMiniPlayerTime(currentPositionMs, durationMs)
            }
            if (formattedPlaybackTime != null) {
                Text(
                    text = formattedPlaybackTime,
                    style = MaterialTheme.typography.labelMedium,
                    color = GolhaColors.SecondaryText,
                )
            }

        }
    }
}

private fun formatMiniPlayerTime(currentPositionMs: Long, durationMs: Long): String? {
    if (durationMs <= 0L) return null
    return "${formatPlaybackTime(durationMs)} / ${formatPlaybackTime(currentPositionMs)}"
}

private fun formatPlaybackTime(timeMs: Long): String {
    val totalSeconds = (timeMs / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
fun BottomNavigationBar(
    items: List<BottomNavItemUiModel>,
    onItemSelected: (AppTab) -> Unit,
) {
    Surface(
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = GolhaColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.8f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onItemSelected(item.tab) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BottomNavItem(item = item)
                }
            }
        }
    }
}

@Composable
fun HeroBannerSkeleton(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .requiredHeight(220.dp),
        shape = RoundedCornerShape(0.dp),
        color = GolhaColors.BannerBackground,
        tonalElevation = GolhaElevation.Banner,
        shadowElevation = GolhaElevation.Banner,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SkeletonBlock(
                    widthFraction = 0.72f,
                    height = 30.dp,
                    color = GolhaColors.BannerDetail.copy(alpha = 0.14f),
                )
                SkeletonBlock(
                    widthFraction = 0.46f,
                    height = 16.dp,
                    color = GolhaColors.BannerDetail.copy(alpha = 0.12f),
                )
                Spacer(modifier = Modifier.height(10.dp))
                SkeletonPill(
                    width = 96.dp,
                    height = 42.dp,
                    color = GolhaColors.BannerDetail.copy(alpha = 0.22f),
                )
            }

            SkeletonRoundedRect(
                width = 136.dp,
                height = 146.dp,
                radius = 30.dp,
                color = GolhaColors.Surface.copy(alpha = 0.75f),
            )
        }
    }
}

@Composable
fun ProgramsSectionSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(GolhaSpacing.Large)) {
        SectionTitle(title = "برنامه‌ها")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GolhaSpacing.CardGap),
            contentPadding = PaddingValues(horizontal = GolhaSpacing.ScreenHorizontal),
        ) {
            items(6) {
                SkeletonRoundedRect(
                    width = 148.dp,
                    height = 92.dp,
                    radius = GolhaRadius.Card,
                )
            }
        }
    }
}

@Composable
fun SingersSectionSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(GolhaSpacing.Large)) {
        SectionTitle(title = "خواننده‌ها")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(horizontal = GolhaSpacing.ScreenHorizontal),
        ) {
            items(6) {
                Column(
                    modifier = Modifier.widthIn(min = 76.dp, max = 88.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SkeletonCircle(size = 78.dp)
                    SkeletonBlock(width = 62.dp, height = 12.dp)
                }
            }
        }
    }
}

@Composable
fun DastgahSectionSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(GolhaSpacing.Large)) {
        SectionTitle(title = "دستگاه‌ها")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = GolhaSpacing.ScreenHorizontal),
        ) {
            items(listOf(74.dp, 88.dp, 64.dp, 84.dp, 58.dp, 102.dp)) { width ->
                SkeletonPill(width = width, height = 40.dp)
            }
        }
    }
}

@Composable
fun MusiciansSectionSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(GolhaSpacing.Large)) {
        SectionTitle(title = "نوازندگان برجسته")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(horizontal = GolhaSpacing.ScreenHorizontal),
        ) {
            items(6) {
                Column(
                    modifier = Modifier.widthIn(min = 76.dp, max = 88.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SkeletonCircle(size = 78.dp, color = GolhaColors.SoftRose.copy(alpha = 0.18f))
                    SkeletonBlock(width = 58.dp, height = 12.dp)
                    SkeletonBlock(width = 38.dp, height = 10.dp)
                }
            }
        }
    }
}

@Composable
fun TopTracksSectionSkeleton() {
    val skeletonCount = 5
    Column(verticalArrangement = Arrangement.spacedBy(GolhaSpacing.Large)) {
        SectionTitle(title = "ترک‌های برتر")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GolhaSpacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(skeletonCount) { index ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SkeletonRoundedRect(width = 58.dp, height = 58.dp, radius = 16.dp)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            SkeletonBlock(widthFraction = 0.72f, height = 14.dp)
                            SkeletonBlock(widthFraction = 0.42f, height = 11.dp)
                        }
                    }

                    SkeletonBlock(width = 36.dp, height = 11.dp)
                    SkeletonCircle(size = 36.dp)
                }

                if (index != skeletonCount - 1) {
                    HorizontalDivider(color = GolhaColors.Border.copy(alpha = 0.65f))
                }
            }
        }
    }
}

@Composable
private fun SkeletonRoundedRect(
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    radius: androidx.compose.ui.unit.Dp,
    color: Color = GolhaColors.Border.copy(alpha = 0.42f),
) {
    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(radius))
            .background(color),
    )
}

@Composable
private fun SkeletonCircle(
    size: androidx.compose.ui.unit.Dp,
    color: Color = GolhaColors.Border.copy(alpha = 0.34f),
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun SkeletonPill(
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    color: Color = GolhaColors.BadgeBackground,
) {
    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(GolhaRadius.Pill))
            .background(color)
            .border(1.dp, GolhaColors.Border.copy(alpha = 0.72f), RoundedCornerShape(GolhaRadius.Pill)),
    )
}

@Composable
private fun SkeletonBlock(
    width: androidx.compose.ui.unit.Dp? = null,
    widthFraction: Float? = null,
    height: androidx.compose.ui.unit.Dp,
    color: Color = GolhaColors.Border.copy(alpha = 0.38f),
) {
    val modifier = when {
        width != null -> Modifier.size(width = width, height = height)
        widthFraction != null -> Modifier.fillMaxWidth(widthFraction).height(height)
        else -> Modifier.fillMaxWidth().height(height)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color),
    )
}

@Composable
fun SmallPrimaryButton(
    label: String,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(GolhaRadius.Pill),
        color = if (enabled) GolhaColors.PrimaryAccent else GolhaColors.Border,
        shadowElevation = if (enabled) 2.dp else 0.dp,
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = GolhaColors.OnAccent,
                    strokeWidth = 2.dp,
                )
            }
            Text(
                text = if (loading) "در حال وارد کردن..." else label,
                color = GolhaColors.OnAccent,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
internal fun CircularActionButton(
    icon: GolhaIcon,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier
            .size(42.dp)
            .clickable { onClick() },
        shape = CircleShape,
        color = GolhaColors.Surface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
    ) {
        Box(contentAlignment = Alignment.Center) {
            GolhaLineIcon(
                icon = icon,
                modifier = Modifier.size(19.dp),
                tint = GolhaColors.PrimaryText,
            )
        }
    }
}

@Composable
private fun PlayPillButton() {
    Surface(
        shape = RoundedCornerShape(GolhaRadius.Pill),
        color = GolhaColors.PrimaryAccent,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GolhaLineIcon(
                icon = GolhaIcon.Play,
                modifier = Modifier.size(16.dp),
                tint = GolhaColors.OnAccent,
            )
            Text(
                text = "پخش",
                color = GolhaColors.OnAccent,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun VintageRadioIllustration() {
    Box(
        modifier = Modifier
            .size(width = 136.dp, height = 146.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(130.dp, 118.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(GolhaColors.Surface.copy(alpha = 0.72f))
                .border(
                    width = 1.dp,
                    color = GolhaColors.BannerDetail.copy(alpha = 0.26f),
                    shape = RoundedCornerShape(30.dp),
                ),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = GolhaColors.SoftSand,
                    size = size,
                    cornerRadius = CornerRadius(30.dp.toPx(), 30.dp.toPx()),
                )
                drawRoundRect(
                    color = GolhaColors.BannerDetail.copy(alpha = 0.24f),
                    topLeft = Offset(size.width * 0.15f, size.height * 0.26f),
                    size = Size(size.width * 0.38f, size.height * 0.38f),
                    cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
                    style = Fill,
                )
                drawCircle(
                    color = GolhaColors.BannerDetail.copy(alpha = 0.30f),
                    radius = size.minDimension * 0.12f,
                    center = Offset(size.width * 0.73f, size.height * 0.38f),
                )
                drawCircle(
                    color = GolhaColors.BannerDetail.copy(alpha = 0.22f),
                    radius = size.minDimension * 0.07f,
                    center = Offset(size.width * 0.84f, size.height * 0.38f),
                )

                val lineColor = GolhaColors.BannerDetail.copy(alpha = 0.42f)
                repeat(4) { index ->
                    val y = size.height * (0.68f + (index * 0.06f))
                    drawLine(
                        color = lineColor,
                        start = Offset(size.width * 0.18f, y),
                        end = Offset(size.width * 0.82f, y),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }

                drawLine(
                    color = lineColor,
                    start = Offset(size.width * 0.28f, size.height * 0.10f),
                    end = Offset(size.width * 0.52f, 0f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = lineColor,
                    start = Offset(size.width * 0.72f, size.height * 0.10f),
                    end = Offset(size.width * 0.48f, 0f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
private fun ProgramCard(
    item: ProgramUiModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(GolhaRadius.Card))
            .clickable { onClick() },
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        tonalElevation = 0.dp,
        shadowElevation = GolhaElevation.Card,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = GolhaColors.PrimaryText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${item.episodeCount} قسمت",
                style = MaterialTheme.typography.bodySmall,
                color = GolhaColors.SecondaryText,
            )
        }
    }
}

@Composable
private fun AvatarNameItem(
    title: String,
    subtitle: String?,
    imageUrl: String?,
    tint: Color,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .widthIn(min = 80.dp, max = 100.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ArtistAvatar(
                name = title,
                imageUrl = imageUrl,
                tint = tint,
                modifier = Modifier.size(78.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
                textAlign = TextAlign.Center,
                color = GolhaColors.PrimaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = GolhaColors.SecondaryText,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun AvatarPlaceholder(
    name: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(GolhaColors.SoftSand.copy(alpha = 0.98f))
            .border(
                width = 1.5.dp,
                color = GolhaColors.PrimaryAccent.copy(alpha = 0.72f),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        GolhaLineIcon(
            icon = GolhaIcon.Profile,
            modifier = Modifier.fillMaxSize(0.5f),
            tint = GolhaColors.PrimaryText.copy(alpha = 0.82f),
        )
    }
}

@Composable
private fun DastgahChip(name: String) {
    Surface(
        shape = RoundedCornerShape(GolhaRadius.Pill),
        color = GolhaColors.BadgeBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = GolhaColors.PrimaryText,
        )
    }
}

// TrackRow and related components moved to ProgramComponents.kt for global consistency

@Composable
private fun SmallCircularIconButton(
    icon: GolhaIcon,
    iconTint: Color,
    background: Color,
    borderColor: Color,
    iconSize: androidx.compose.ui.unit.Dp = 14.dp,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .size(36.dp)
            .clickable { onClick() },
        shape = CircleShape,
        color = background,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        Box(contentAlignment = Alignment.Center) {
            GolhaLineIcon(
                icon = icon,
                modifier = Modifier.size(iconSize),
                tint = iconTint,
            )
        }
    }
}

@Composable
private fun BottomNavItem(item: BottomNavItemUiModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        GolhaLineIcon(
            icon = item.icon,
            modifier = Modifier.size(26.dp),
            tint = if (item.selected) GolhaColors.PrimaryAccent else GolhaColors.SecondaryText,
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (item.selected) FontWeight.Medium else FontWeight.Normal
            ),
            color = if (item.selected) GolhaColors.PrimaryAccent else GolhaColors.SecondaryText,
        )
    }
}

@Composable
fun GolhaLineIcon(
    icon: GolhaIcon,
    modifier: Modifier = Modifier,
    tint: Color,
) {
    Canvas(modifier = modifier) {
        val stroke = 2.4.dp.toPx()
        when (icon) {
            GolhaIcon.Favorites -> {
                val heart = Path().apply {
                    moveTo(size.width * 0.50f, size.height * 0.82f)
                    cubicTo(
                        size.width * 0.18f,
                        size.height * 0.60f,
                        size.width * 0.14f,
                        size.height * 0.26f,
                        size.width * 0.35f,
                        size.height * 0.22f,
                    )
                    cubicTo(
                        size.width * 0.48f,
                        size.height * 0.22f,
                        size.width * 0.50f,
                        size.height * 0.34f,
                        size.width * 0.50f,
                        size.height * 0.34f,
                    )
                    cubicTo(
                        size.width * 0.50f,
                        size.height * 0.34f,
                        size.width * 0.52f,
                        size.height * 0.22f,
                        size.width * 0.65f,
                        size.height * 0.22f,
                    )
                    cubicTo(
                        size.width * 0.86f,
                        size.height * 0.26f,
                        size.width * 0.82f,
                        size.height * 0.60f,
                        size.width * 0.50f,
                        size.height * 0.82f,
                    )
                    close()
                }
                drawPath(heart, tint, style = Stroke(width = stroke))
            }

            GolhaIcon.Profile,
            GolhaIcon.Account -> {
                // Head
                drawCircle(
                    color = tint,
                    radius = size.minDimension * 0.18f,
                    center = Offset(size.width * 0.50f, size.height * 0.28f),
                    style = Stroke(width = stroke)
                )
                // Shoulders / Chest
                val body = Path().apply {
                    moveTo(size.width * 0.18f, size.height * 0.82f)
                    cubicTo(
                        size.width * 0.18f, size.height * 0.62f,
                        size.width * 0.32f, size.height * 0.52f,
                        size.width * 0.50f, size.height * 0.52f
                    )
                    cubicTo(
                        size.width * 0.68f, size.height * 0.52f,
                        size.width * 0.82f, size.height * 0.62f,
                        size.width * 0.82f, size.height * 0.82f
                    )
                }
                drawPath(body, tint, style = Stroke(width = stroke, cap = StrokeCap.Round))
            }

            GolhaIcon.Home -> {
                // Full house outline as one connected path
                val house = Path().apply {
                    moveTo(size.width * 0.50f, size.height * 0.10f)  // peak
                    lineTo(size.width * 0.93f, size.height * 0.48f)  // roof right end
                    lineTo(size.width * 0.82f, size.height * 0.48f)  // step in to right wall
                    lineTo(size.width * 0.82f, size.height * 0.88f)  // right wall down
                    lineTo(size.width * 0.18f, size.height * 0.88f)  // base
                    lineTo(size.width * 0.18f, size.height * 0.48f)  // left wall up
                    lineTo(size.width * 0.07f, size.height * 0.48f)  // roof left end
                    close()
                }
                drawPath(house, tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
                // Door
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.37f, size.height * 0.60f),
                    size = Size(size.width * 0.26f, size.height * 0.28f),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    style = Stroke(width = stroke),
                )
            }

            GolhaIcon.Search -> {
                drawCircle(
                    color = tint,
                    radius = size.minDimension * 0.24f,
                    center = Offset(size.width * 0.42f, size.height * 0.42f),
                    style = Stroke(width = stroke),
                )
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.58f, size.height * 0.58f),
                    end = Offset(size.width * 0.82f, size.height * 0.82f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }

            GolhaIcon.Library -> {
                repeat(3) { index ->
                    val y = size.height * (0.24f + index * 0.22f)
                    drawRoundRect(
                        color = tint,
                        topLeft = Offset(size.width * 0.20f, y),
                        size = Size(size.width * 0.60f, size.height * 0.12f),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        style = Stroke(width = stroke),
                    )
                }
            }

            GolhaIcon.Play -> {
                val triangle = Path().apply {
                    moveTo(size.width * 0.32f, size.height * 0.24f)
                    lineTo(size.width * 0.75f, size.height * 0.50f)
                    lineTo(size.width * 0.32f, size.height * 0.76f)
                    close()
                }
                drawPath(triangle, tint)
            }

            GolhaIcon.Pause -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.26f, size.height * 0.22f),
                    size = Size(size.width * 0.16f, size.height * 0.56f),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                )
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.58f, size.height * 0.22f),
                    size = Size(size.width * 0.16f, size.height * 0.56f),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                )
            }

            GolhaIcon.SkipPrevious -> {
                val triangle = Path().apply {
                    moveTo(size.width * 0.75f, size.height * 0.28f)
                    lineTo(size.width * 0.35f, size.height * 0.50f)
                    lineTo(size.width * 0.75f, size.height * 0.72f)
                    close()
                }
                drawPath(triangle, tint)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.22f, size.height * 0.28f),
                    size = Size(size.width * 0.08f, size.height * 0.44f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                )
            }

            GolhaIcon.SkipNext -> {
                val triangle = Path().apply {
                    moveTo(size.width * 0.25f, size.height * 0.28f)
                    lineTo(size.width * 0.65f, size.height * 0.50f)
                    lineTo(size.width * 0.25f, size.height * 0.72f)
                    close()
                }
                drawPath(triangle, tint)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.70f, size.height * 0.28f),
                    size = Size(size.width * 0.08f, size.height * 0.44f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                )
            }

            GolhaIcon.More -> {
                repeat(3) { index ->
                    drawCircle(
                        color = tint,
                        radius = size.minDimension * 0.08f,
                        center = Offset(size.width * (0.28f + index * 0.22f), size.height * 0.50f),
                    )
                }
            }

            GolhaIcon.Download -> {
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.50f, size.height * 0.18f),
                    end = Offset(size.width * 0.50f, size.height * 0.60f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                val arrow = Path().apply {
                    moveTo(size.width * 0.34f, size.height * 0.48f)
                    lineTo(size.width * 0.50f, size.height * 0.66f)
                    lineTo(size.width * 0.66f, size.height * 0.48f)
                }
                drawPath(arrow, tint, style = Stroke(width = stroke, cap = StrokeCap.Round))
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.24f, size.height * 0.78f),
                    end = Offset(size.width * 0.76f, size.height * 0.78f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }

            GolhaIcon.Back -> {
                val path = Path().apply {
                    moveTo(size.width * 0.62f, size.height * 0.32f)
                    lineTo(size.width * 0.42f, size.height * 0.50f)
                    lineTo(size.width * 0.62f, size.height * 0.68f)
                }
                drawPath(path, tint, style = Stroke(width = stroke, cap = StrokeCap.Round))
            }

            GolhaIcon.Refresh -> {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = size.minDimension * 0.30f
                val sw = 1.6.dp.toPx()

                // Circular loop
                drawArc(
                    color = tint,
                    startAngle = 150f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(r * 2, r * 2),
                    style = Stroke(width = sw, cap = StrokeCap.Round)
                )

                // Arrowhead at the end of the arc (60 degrees)
                val angle = 60f * (3.14159f / 180f)
                val ax = cx + r * kotlin.math.cos(angle)
                val ay = cy + r * kotlin.math.sin(angle)
                
                val hl = r * 0.5f
                val arrowPath = Path().apply {
                    moveTo(ax - hl * 0.8f, ay + hl * 0.1f)
                    lineTo(ax, ay)
                    lineTo(ax - hl * 0.1f, ay - hl * 0.8f)
                }
                drawPath(arrowPath, tint, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }

            GolhaIcon.Timer -> {
                drawCircle(color = tint, radius = size.minDimension * 0.32f, style = Stroke(width = stroke))
                drawLine(color = tint, start = center, end = Offset(center.x, center.y - size.height * 0.2f), strokeWidth = stroke)
                drawLine(color = tint, start = center, end = Offset(center.x + size.width * 0.15f, center.y), strokeWidth = stroke)
            }

            GolhaIcon.Note -> {
                drawRoundRect(color = tint, style = Stroke(width = stroke), cornerRadius = CornerRadius(4.dp.toPx()))
                drawLine(color = tint, start = Offset(size.width * 0.3f, size.height * 0.3f), end = Offset(size.width * 0.7f, size.height * 0.3f), strokeWidth = stroke)
                drawLine(color = tint, start = Offset(size.width * 0.3f, size.height * 0.5f), end = Offset(size.width * 0.7f, size.height * 0.5f), strokeWidth = stroke)
            }

            GolhaIcon.History -> {
                drawCircle(color = tint, radius = size.minDimension * 0.35f, style = Stroke(width = stroke))
                drawLine(color = tint, start = center, end = Offset(center.x, center.y - size.height * 0.18f), strokeWidth = stroke)
                drawLine(color = tint, start = center, end = Offset(center.x + size.width * 0.12f, center.y + size.width * 0.12f), strokeWidth = stroke)
            }

            GolhaIcon.People -> {
                drawCircle(color = tint, center = Offset(size.width * 0.35f, size.height * 0.35f), radius = size.minDimension * 0.15f, style = Stroke(width = stroke))
                drawCircle(color = tint, center = Offset(size.width * 0.65f, size.height * 0.35f), radius = size.minDimension * 0.15f, style = Stroke(width = stroke))
            }

            GolhaIcon.Info -> {
                // Outer circle
                drawCircle(color = tint, radius = size.minDimension * 0.44f, style = Stroke(width = stroke))
                // Dot
                drawCircle(color = tint, radius = stroke * 0.7f, center = Offset(center.x, size.height * 0.32f))
                // Vertical stem
                drawLine(
                    color = tint,
                    start = Offset(center.x, size.height * 0.46f),
                    end = Offset(center.x, size.height * 0.72f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }

            GolhaIcon.SeekBack10 -> {
                // Arrow arc pointing left (counter-clockwise)
                drawArc(
                    color = tint,
                    startAngle = -30f,
                    sweepAngle = -300f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.18f, size.height * 0.18f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.64f),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                // Arrowhead pointing left at the end of arc
                val ax = size.width * 0.34f
                val ay = size.height * 0.14f
                val arrowPath = Path().apply {
                    moveTo(ax - size.width * 0.12f, ay + size.height * 0.05f)
                    lineTo(ax, ay)
                    lineTo(ax + size.width * 0.04f, ay + size.height * 0.12f)
                }
                drawPath(arrowPath, tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
                // "10" text approximated as two lines
                // digit "1"
                drawLine(tint, Offset(size.width * 0.38f, size.height * 0.42f), Offset(size.width * 0.38f, size.height * 0.62f), stroke, StrokeCap.Round)
                // digit "0" as small rect/oval approximation
                drawArc(
                    color = tint,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.46f, size.height * 0.42f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.18f, size.height * 0.20f),
                    style = Stroke(width = stroke * 0.85f, cap = StrokeCap.Round)
                )
            }

            GolhaIcon.SeekForward10 -> {
                // Arrow arc pointing right (clockwise)
                drawArc(
                    color = tint,
                    startAngle = -150f,
                    sweepAngle = 300f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.18f, size.height * 0.18f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.64f),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                // Arrowhead pointing right
                val ax = size.width * 0.66f
                val ay = size.height * 0.14f
                val arrowPath = Path().apply {
                    moveTo(ax + size.width * 0.12f, ay + size.height * 0.05f)
                    lineTo(ax, ay)
                    lineTo(ax - size.width * 0.04f, ay + size.height * 0.12f)
                }
                drawPath(arrowPath, tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
                // "10" text
                // digit "1"
                drawLine(tint, Offset(size.width * 0.38f, size.height * 0.42f), Offset(size.width * 0.38f, size.height * 0.62f), stroke, StrokeCap.Round)
                // digit "0"
                drawArc(
                    color = tint,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.46f, size.height * 0.42f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.18f, size.height * 0.20f),
                    style = Stroke(width = stroke * 0.85f, cap = StrokeCap.Round)
                )
            }
        }
    }
}
