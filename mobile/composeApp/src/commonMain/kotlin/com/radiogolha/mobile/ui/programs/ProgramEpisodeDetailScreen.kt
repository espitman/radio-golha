package com.radiogolha.mobile.ui.programs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import com.radiogolha.mobile.theme.*
import com.radiogolha.mobile.ui.home.*
import com.radiogolha.mobile.ui.root.TabRootScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import radiogolha_mobile.composeapp.generated.resources.Res
import radiogolha_mobile.composeapp.generated.resources.eslimi_card_bg

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProgramEpisodeDetailScreen(
    programId: Long,
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    onBackClick: () -> Unit,
    currentTrack: TrackUiModel? = null,
    isPlayerPlaying: Boolean = false,
    isPlayerLoading: Boolean = false,
    currentPlaybackPositionMs: Long = 0L,
    currentPlaybackDurationMs: Long = 0L,
    onTogglePlayerPlayback: () -> Unit = {},
    onPlayProgram: (ProgramEpisodeDetailUiModel) -> Unit = {},
    onArtistClick: (Long) -> Unit = {},
    onTrackClick: (Long) -> Unit = {},
    onExpandPlayer: () -> Unit = {},
    onSeek: (Long) -> Unit = {},
) {
    var detail by remember { mutableStateOf<ProgramEpisodeDetailUiModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var activeTab by remember { mutableStateOf(DetailTab.Timeline) }

    LaunchedEffect(programId) {
        isLoading = true
        detail = withContext(Dispatchers.Default) {
             loadProgramEpisodeDetail(programId)
        }
        isLoading = false
    }

    val scrollState = rememberLazyListState()
    val density = LocalDensity.current
    
    // Threshold for header collapse
    val headerThreshold = with(density) { 120.dp.toPx() }
    val collapseProgress by remember {
        derivedStateOf {
            if (scrollState.firstVisibleItemIndex > 0) 1f
            else (scrollState.firstVisibleItemScrollOffset / headerThreshold).coerceIn(0f, 1f)
        }
    }

    // Dynamic height of the header for stacking
    val collapsedHeaderHeight = 56.dp + 12.dp // Header height + small gap

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        TabRootScreen(
            title = "برنامه",
            subtitle = detail?.categoryName ?: "",
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
            headerOverlay = {
                val isPlaying = currentTrack?.id == detail?.id && isPlayerPlaying
                ProgramHeaderLayout(
                    detail = detail,
                    isLoading = isLoading,
                    progress = collapseProgress,
                    isPlaying = isPlaying,
                    onPlayClick = { detail?.let { onPlayProgram(it) } }
                )
            },
            content = {
                val resolvedDetail = detail
                
                // 1. Placeholder for the Expanded Header
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }

                if (isLoading) {
                    item { DetailSkeleton() }
                } else if (detail == null) {
                    item { ErrorState() }
                } else {
                    val d = detail!!
                    
                    // 2. Info Grid
                    item { InfoGrid(detail = d) }

                    // 3. Carousels
                    if (d.singers.isNotEmpty()) item { ArtistCarousel(title = "خوانندگان", artists = d.singers, onArtistClick = onArtistClick) }
                    if (d.orchestras.isNotEmpty()) item { ArtistCarousel(title = "ارکسترها", artists = d.orchestras, onArtistClick = onArtistClick) }
                    if (d.orchestraLeaders.isNotEmpty()) {
                        val leaders = d.orchestraLeaders.map { ArtistCreditUiModel(it.artistId, it.name, null) }
                        item { ArtistCarousel(title = "رهبران ارکستر", artists = leaders, onArtistClick = onArtistClick, subtitleGetter = { l -> d.orchestraLeaders.find { it.name == l.name }?.orchestra }) }
                    }
                    if (d.performers.isNotEmpty()) {
                        val musicians = d.performers.map { ArtistCreditUiModel(it.artistId, it.name, it.avatar) }
                        item { ArtistCarousel(title = "نوازندگان", artists = musicians, onArtistClick = onArtistClick, subtitleGetter = { m -> d.performers.find { it.name == m.name }?.instrument ?: "نوازنده" }) }
                    }

                    // 4. Stacking Sticky Tabs
                    val hasTimeline = d.timeline.isNotEmpty()
                    val hasLyrics = d.transcript.isNotEmpty()

                    val isThisTrackPlaying = currentTrack?.id == d.id

                    if (hasTimeline && hasLyrics) {
                        stickyHeader {
                            DetailTabSelector(
                                activeTab = activeTab,
                                onTabChange = { activeTab = it },
                                stickyPadding = collapsedHeaderHeight,
                                isCollapsed = scrollState.firstVisibleItemIndex >= 1
                            )
                        }

                        when (activeTab) {
                            DetailTab.Timeline -> TimelineSection(
                                timeline = d.timeline,
                                currentPositionMs = if (isThisTrackPlaying) currentPlaybackPositionMs else -1L,
                                onSegmentClick = { startMs -> if (isThisTrackPlaying) onSeek(startMs) else { onPlayProgram(d); onSeek(startMs) } }
                            )
                            DetailTab.Lyrics -> LyricsSection(transcript = d.transcript)
                        }
                    } else if (hasTimeline) {
                        item { SectionTitle(title = "تایم‌لاین") }
                        TimelineSection(
                            timeline = d.timeline,
                            currentPositionMs = if (isThisTrackPlaying) currentPlaybackPositionMs else -1L,
                            onSegmentClick = { startMs -> if (isThisTrackPlaying) onSeek(startMs) else { onPlayProgram(d); onSeek(startMs) } }
                        )
                    } else if (hasLyrics) {
                        item { SectionTitle(title = "متن و اشعار") }
                        LyricsSection(transcript = d.transcript)
                    }
                    
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        )
    }
}

@Composable
private fun ProgramHeaderLayout(
    detail: ProgramEpisodeDetailUiModel? = null,
    isLoading: Boolean = false,
    progress: Float = 0f,
    isPlaying: Boolean = false,
    onPlayClick: () -> Unit = {}
) {
    val currentPadding = lerp(18.dp, 10.dp, progress)
    val titleFontSize = lerp(20.sp, 17.sp, progress)
    val isSticky = progress > 0.95f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = if (isSticky) 0.dp else GolhaSpacing.ScreenHorizontal)
            .padding(top = if (isSticky) 0.dp else 4.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = if (isSticky) RoundedCornerShape(0.dp) else RoundedCornerShape(GolhaRadius.Card),
            color = GolhaColors.Surface,
            border = if (isSticky) null else androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.7f)),
            shadowElevation = if (isSticky) 6.dp else 0.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.Image(
                    painter = painterResource(Res.drawable.eslimi_card_bg),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize().alpha(0.32f),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    GolhaColors.Surface.copy(alpha = 0.92f),
                                    GolhaColors.Surface.copy(alpha = 0.4f + (0.2f * progress))
                                )
                            )
                        )
                )

                if (progress < 0.8f) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(currentPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (isLoading || detail == null) {
                            Box(modifier = Modifier.width(200.dp).height(26.dp).background(GolhaColors.Border.copy(alpha = 0.3f), RoundedCornerShape(4.dp)))
                            Box(modifier = Modifier.fillMaxWidth(0.7f).height(54.dp).background(GolhaColors.Border.copy(alpha = 0.2f), RoundedCornerShape(GolhaRadius.Pill)))
                        } else {
                            Text(
                                text = detail.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = titleFontSize),
                                color = GolhaColors.PrimaryText,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )
                            Button(
                                onClick = onPlayClick,
                                modifier = Modifier.fillMaxWidth(0.7f).height(54.dp),
                                shape = RoundedCornerShape(GolhaRadius.Pill),
                                colors = ButtonDefaults.buttonColors(containerColor = GolhaColors.PrimaryAccent)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    GolhaLineIcon(icon = if (isPlaying) GolhaIcon.Pause else GolhaIcon.Play, modifier = Modifier.size(24.dp), tint = Color.White)
                                    Text(text = if (isPlaying) "توقف پخش" else "پخش برنامه", style = MaterialTheme.typography.titleMedium, color = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularPlayerButton(isPlaying = isPlaying, onClick = onPlayClick, modifier = Modifier.size(40.dp))
                        Text(
                            modifier = Modifier.weight(1f),
                            text = detail?.title ?: "در حال بارگذاری...",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GolhaColors.PrimaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTabSelector(
    activeTab: DetailTab, 
    onTabChange: (DetailTab) -> Unit,
    stickyPadding: Dp = 0.dp,
    isCollapsed: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = if (isCollapsed) stickyPadding else 0.dp),
        color = GolhaColors.ScreenBackground,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = GolhaSpacing.ScreenHorizontal, vertical = 12.dp),
            color = GolhaColors.Surface,
            shape = RoundedCornerShape(GolhaRadius.Pill),
            border = if (isCollapsed) null else androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.5f)),
            shadowElevation = if (isCollapsed) 4.dp else 0.dp
        ) {
            Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                DetailTab.entries.forEach { tab ->
                    val selected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(GolhaRadius.Pill))
                            .background(if (selected) GolhaColors.PrimaryAccent else Color.Transparent)
                            .clickable { onTabChange(tab) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (selected) Color.White else GolhaColors.SecondaryText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CircularPlayerButton(isPlaying: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = CircleShape,
        color = GolhaColors.PrimaryAccent,
        tonalElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            GolhaLineIcon(icon = if (isPlaying) GolhaIcon.Pause else GolhaIcon.Play, modifier = Modifier.size(20.dp), tint = Color.White)
        }
    }
}

private fun lerp(start: Dp, end: Dp, fraction: Float): Dp = start + (end - start) * fraction
private fun lerp(start: androidx.compose.ui.unit.TextUnit, end: androidx.compose.ui.unit.TextUnit, fraction: Float): androidx.compose.ui.unit.TextUnit = (start.value + (end.value - start.value) * fraction).sp

private enum class DetailTab(val label: String) { Timeline("تایم‌لاین"), Lyrics("اشعار") }

private fun parseTimeToMs(time: String?): Long {
    if (time == null) return 0L
    return try {
        val parts = time.trim().split(":")
        when (parts.size) {
            2 -> (parts[0].toLong() * 60 + parts[1].toLong()) * 1000L
            3 -> (parts[0].toLong() * 3600 + parts[1].toLong() * 60 + parts[2].toLong()) * 1000L
            else -> 0L
        }
    } catch (e: Exception) { 0L }
}

private fun androidx.compose.foundation.lazy.LazyListScope.TimelineSection(
    timeline: List<TimelineSegmentUiModel>,
    currentPositionMs: Long = -1L,
    onSegmentClick: (Long) -> Unit = {},
) {
    items(timeline.size) { index ->
        val segment = timeline[index]
        val startMs = parseTimeToMs(segment.startTime)
        val endMs = if (index + 1 < timeline.size) parseTimeToMs(timeline[index + 1].startTime) else Long.MAX_VALUE
        val isActive = currentPositionMs >= 0L && currentPositionMs >= startMs && currentPositionMs < endMs
        TimelineSegmentCard(segment = segment, isActive = isActive, onClick = { onSegmentClick(startMs) })
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun TimelineSegmentCard(
    segment: TimelineSegmentUiModel,
    isActive: Boolean = false,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = if (isActive) GolhaColors.PrimaryAccent.copy(alpha = 0.08f) else GolhaColors.Surface.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) GolhaColors.PrimaryAccent.copy(alpha = 0.6f) else GolhaColors.Border.copy(alpha = 0.6f)
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = if (isActive) GolhaColors.PrimaryAccent else GolhaColors.PrimaryAccent.copy(alpha = 0.1f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isActive) {
                            GolhaLineIcon(icon = GolhaIcon.Play, modifier = Modifier.size(14.dp), tint = Color.White)
                        } else {
                            Text(
                                text = segment.startTime ?: "0:00",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                                color = GolhaColors.PrimaryAccent
                            )
                        }
                    }
                }
                Box(modifier = Modifier.width(2.dp).weight(1f).background(
                    if (isActive) GolhaColors.PrimaryAccent.copy(alpha = 0.4f) else GolhaColors.Border.copy(alpha = 0.5f)
                ))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = segment.modeName ?: "بخش اجرایی",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isActive) GolhaColors.PrimaryAccent else GolhaColors.PrimaryText
                )
                if (segment.singers.isNotEmpty()) TimelineMetaRow(icon = GolhaIcon.People, text = segment.singers.joinToString(" و "))
                if (segment.poets.isNotEmpty()) TimelineMetaRow(icon = GolhaIcon.Note, text = segment.poets.joinToString(" و "))
                if (segment.performers.isNotEmpty()) {
                    val performersText = segment.performers.joinToString(" و ") { "${it.name} (${it.instrument ?: "نوازنده"})" }
                    TimelineMetaRow(icon = GolhaIcon.Note, text = performersText)
                }
            }
        }
    }
}

@Composable
private fun TimelineMetaRow(icon: GolhaIcon, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        GolhaLineIcon(icon = icon, modifier = Modifier.size(14.dp), tint = GolhaColors.SecondaryText.copy(alpha = 0.7f))
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = GolhaColors.SecondaryText)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.LyricsSection(transcript: List<TranscriptVerseUiModel>) {
    item { TranscriptCard(transcript = transcript) }
}

@Composable
private fun TranscriptCard(transcript: List<TranscriptVerseUiModel>) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(GolhaRadius.Card), color = GolhaColors.Surface, border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.7f))) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            transcript.forEach { verse -> Text(text = verse.text, style = MaterialTheme.typography.bodyLarge, color = GolhaColors.PrimaryText, textAlign = TextAlign.Center) }
        }
    }
}

@Composable
private fun InfoGrid(detail: ProgramEpisodeDetailUiModel) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoTile(modifier = Modifier.weight(1f), label = "دستگاه/آواز", value = detail.modes.joinToString("، "), icon = GolhaIcon.Note)
        InfoTile(modifier = Modifier.weight(1f), label = "زمان", value = detail.duration ?: "نامشخص", icon = GolhaIcon.Timer)
    }
}

@Composable
private fun InfoTile(modifier: Modifier, label: String, value: String, icon: GolhaIcon) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = GolhaColors.BadgeBackground.copy(alpha = 0.3f), border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.5f))) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GolhaLineIcon(icon = icon, modifier = Modifier.size(14.dp), tint = GolhaColors.SecondaryText)
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = GolhaColors.SecondaryText)
            }
            Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = GolhaColors.PrimaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ArtistCarousel(title: String, artists: List<ArtistCreditUiModel>, onArtistClick: (Long) -> Unit = {}, subtitleGetter: ((ArtistCreditUiModel) -> String?)? = null) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        SectionTitle(title = title)
        Spacer(modifier = Modifier.height(14.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = GolhaSpacing.ScreenHorizontal), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(artists) { artist ->
                ArtistCarouselItem(artist = artist, subtitle = subtitleGetter?.invoke(artist), onClick = { artist.artistId?.let { onArtistClick(it) } })
            }
        }
    }
}

@Composable
private fun ArtistCarouselItem(artist: ArtistCreditUiModel, subtitle: String? = null, onClick: () -> Unit = {}) {
    Column(modifier = Modifier.width(88.dp).clickable { onClick() }.padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ArtistAvatar(name = artist.name, imageUrl = artist.avatar, tint = GolhaColors.SoftRose, modifier = Modifier.size(80.dp))
        Text(text = artist.name, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        if (subtitle != null) Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = GolhaColors.SecondaryText, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}

@Composable
private fun DetailSkeleton() {
    Column(modifier = Modifier.padding(horizontal = GolhaSpacing.ScreenHorizontal), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.fillMaxWidth().height(130.dp).background(GolhaColors.Surface.copy(alpha = 0.5f), RoundedCornerShape(GolhaRadius.Card)))
    }
}

@Composable
private fun ErrorState() {
    Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
        Text("خطا در بارگذاری اطلاعات برنامه", color = GolhaColors.SecondaryText)
    }
}
