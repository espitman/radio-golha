package com.radiogolha.mobile.tv

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.radiogolha.mobile.rememberGolhaPlayer
import com.radiogolha.mobile.theme.GolhaAppTheme
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaPatternBackground
import com.radiogolha.mobile.ui.home.DastgahUiModel
import com.radiogolha.mobile.ui.home.DuetPairUiModel
import com.radiogolha.mobile.ui.home.MusicianUiModel
import com.radiogolha.mobile.ui.home.ProgramUiModel
import com.radiogolha.mobile.ui.home.SingerUiModel
import com.radiogolha.mobile.ui.home.TrackUiModel
import com.radiogolha.mobile.ui.home.loadHomeUiState
import com.radiogolha.mobile.ui.home.loadDuetPairsConfig
import com.radiogolha.mobile.ui.home.loadTopTracks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class TvTopMenuItem(val title: String) {
    FavoriteSingers("خواننده‌های مورد علاقه"),
    FavoritePlayers("نوازندگان مورد علاقه"),
    MyPlaylists("پلی لیست‌های من"),
    TopPrograms("محبوب‌ترین برنامه‌ها"),
}

private enum class TvSideMenuItem(val title: String, val icon: ImageVector) {
    Home("صفحه اصلی", Icons.Filled.Home),
    Singers("خواننده‌ها", Icons.Filled.Person),
    Players("نوازندگان", Icons.Filled.PlayArrow),
    Recent("شنیده‌شده‌های اخیر", Icons.Filled.DateRange),
    Search("جستجوی پیشرفته", Icons.Filled.Search),
    Settings("تنظیمات", Icons.Filled.Settings),
    Help("راهنما", Icons.Filled.Info),
}

@Composable
fun TvApp() {
    GolhaAppTheme {
        val player = rememberGolhaPlayer()
        val currentTrack by player.currentTrack.collectAsState()
        val isPlayerPlaying by player.isPlaying.collectAsState()
        val isPlayerLoading by player.isLoading.collectAsState()
        val currentPositionMs by player.currentPositionMs.collectAsState()
        val durationMs by player.durationMs.collectAsState()

        // Keep the shell geometry predictable, but force content RTL.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            GolhaPatternBackground {
                var selectedTop by remember { mutableStateOf<TvTopMenuItem?>(null) }
                var selectedSide by remember { mutableStateOf(TvSideMenuItem.Home) }
                var selectedArtistId by remember { mutableStateOf<Long?>(null) }

                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val topFocusRequesters = remember {
                        TvTopMenuItem.entries.map { FocusRequester() }
                    }
                    val sidebarFocusRequesters = remember {
                        TvSideMenuItem.entries.map { FocusRequester() }
                    }
                    val duetFocusRequester = remember { FocusRequester() }
                    val programFocusRequester = remember { FocusRequester() }
                    val modeFocusRequester = remember { FocusRequester() }
                    val singerFocusRequester = remember { FocusRequester() }
                    val musicianFocusRequester = remember { FocusRequester() }
                    val trackRefreshFocusRequester = remember { FocusRequester() }
                    val trackFocusRequester = remember { FocusRequester() }
                    val topTracksLastFocusRequester = remember { FocusRequester() }
                    val playerFocusRequester = remember { FocusRequester() }
                    val mainEntryRequester = topFocusRequesters.first()
                    val sidebarEntryRequester = sidebarFocusRequesters.first()

                    LaunchedEffect(sidebarEntryRequester) {
                        delay(100)
                        sidebarEntryRequester.requestFocus()
                    }

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        TvMainPane(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            selectedTop = selectedTop,
                            selectedArtistId = selectedArtistId,
                            onSelectTop = { selectedTop = it; selectedSide = TvSideMenuItem.Home; selectedArtistId = null },
                            canGoBack = selectedArtistId != null || selectedTop != null || selectedSide != TvSideMenuItem.Home,
                            onBack = {
                                if (selectedArtistId != null) {
                                    selectedArtistId = null
                                } else {
                                    selectedTop = null
                                    selectedSide = TvSideMenuItem.Home
                                }
                            },
                            onOpenArtist = { selectedArtistId = it },
                            focusRequesters = topFocusRequesters,
                            duetFocusRequester = duetFocusRequester,
                            programFocusRequester = programFocusRequester,
                            modeFocusRequester = modeFocusRequester,
                            singerFocusRequester = singerFocusRequester,
                            musicianFocusRequester = musicianFocusRequester,
                            trackRefreshFocusRequester = trackRefreshFocusRequester,
                            trackFocusRequester = trackFocusRequester,
                            topTracksLastFocusRequester = topTracksLastFocusRequester,
                            playerFocusRequester = playerFocusRequester,
                            sidebarEntryRequester = sidebarEntryRequester,
                            currentTrack = currentTrack,
                            isPlayerPlaying = isPlayerPlaying,
                            isPlayerLoading = isPlayerLoading,
                            currentPositionMs = currentPositionMs,
                            durationMs = durationMs,
                            onPlayTrack = { player.play(it) },
                            onTogglePlayerPlayback = { player.togglePlayback() },
                        )
                    }
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        TvSidebar(
                            selectedItem = selectedSide,
                            onSelectItem = { item ->
                                selectedSide = item
                                selectedTop = null
                                selectedArtistId = null
                            },
                            focusRequesters = sidebarFocusRequesters,
                            mainEntryRequester = mainEntryRequester,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvMainPane(
    modifier: Modifier = Modifier,
    selectedTop: TvTopMenuItem?,
    selectedArtistId: Long?,
    onSelectTop: (TvTopMenuItem) -> Unit,
    canGoBack: Boolean,
    onBack: () -> Unit,
    onOpenArtist: (Long) -> Unit,
    focusRequesters: List<FocusRequester>,
    duetFocusRequester: FocusRequester,
    programFocusRequester: FocusRequester,
    modeFocusRequester: FocusRequester,
    singerFocusRequester: FocusRequester,
    musicianFocusRequester: FocusRequester,
    trackRefreshFocusRequester: FocusRequester,
    trackFocusRequester: FocusRequester,
    topTracksLastFocusRequester: FocusRequester,
    playerFocusRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
    currentTrack: TrackUiModel?,
    isPlayerPlaying: Boolean,
    isPlayerLoading: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onPlayTrack: (TrackUiModel) -> Unit,
    onTogglePlayerPlayback: () -> Unit,
) {
    var duetItems by remember { mutableStateOf<List<DuetPairUiModel>>(emptyList()) }
    var programItems by remember { mutableStateOf<List<ProgramUiModel>>(emptyList()) }
    var modeItems by remember { mutableStateOf<List<DastgahUiModel>>(emptyList()) }
    var singerItems by remember { mutableStateOf<List<SingerUiModel>>(emptyList()) }
    var musicianItems by remember { mutableStateOf<List<MusicianUiModel>>(emptyList()) }
    var topTrackItems by remember { mutableStateOf<List<TrackUiModel>>(emptyList()) }
    var isHomeLoading by remember { mutableStateOf(true) }
    var isTopTracksRefreshing by remember { mutableStateOf(false) }
    var topTracksRefreshNonce by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        isHomeLoading = true
        val home = withContext(Dispatchers.Default) {
            runCatching { loadHomeUiState() }
                .onFailure { println("ERROR TvMainPane loadHomeUiState: ${it.message}") }
                .getOrNull()
        }
        val homeDuets = home?.duets.orEmpty()
        val configDuets = if (homeDuets.isEmpty()) {
            withContext(Dispatchers.Default) {
                runCatching { loadDuetPairsConfig() }
                    .onFailure { println("ERROR TvMainPane loadDuetPairsConfig: ${it.message}") }
                    .getOrDefault(emptyList())
            }
        } else {
            emptyList()
        }
        programItems = home?.programs.orEmpty().sortedByDescending { it.episodeCount }
        modeItems = home?.dastgahs.orEmpty()
        singerItems = home?.singers.orEmpty()
        musicianItems = home?.musicians.orEmpty()
        topTrackItems = home?.topTracks.orEmpty()
        duetItems = homeDuets.ifEmpty { configDuets }
        println("TV_HOME_LOADED programs=${programItems.size} modes=${modeItems.size} singers=${singerItems.size} musicians=${musicianItems.size} topTracks=${topTrackItems.size} duets=${duetItems.size}")
        isHomeLoading = false
    }

    LaunchedEffect(topTracksRefreshNonce) {
        if (topTracksRefreshNonce == 0) return@LaunchedEffect
        isTopTracksRefreshing = true
        topTrackItems = withContext(Dispatchers.Default) {
            runCatching { loadTopTracks() }
                .onFailure { println("ERROR TvMainPane loadTopTracks: ${it.message}") }
                .getOrDefault(emptyList())
        }
        println("TV_TOP_TRACKS_REFRESHED count=${topTrackItems.size}")
        isTopTracksRefreshing = false
        trackFocusRequester.requestFocus()
    }

    val artistEntryFocusRequester = remember { FocusRequester() }
    val artistLastTrackFocusRequester = remember { FocusRequester() }
    val mainDownFocusRequester = when {
        selectedArtistId != null -> artistEntryFocusRequester
        selectedTop == null -> duetFocusRequester
        else -> playerFocusRequester
    }
    val playerUpFocusRequester = when {
        selectedArtistId != null -> artistLastTrackFocusRequester
        selectedTop == null -> if (topTrackItems.take(5).size > 1) topTracksLastFocusRequester else trackFocusRequester
        else -> focusRequesters.first()
    }
    val focusScope = rememberCoroutineScope()
    val playTrackAndFocusPlayer: (TrackUiModel) -> Unit = { track ->
        onPlayTrack(track)
        focusScope.launch {
            delay(80)
            playerFocusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
    ) {
        TvTopMenuBar(
            selectedTop = selectedTop,
            onSelect = onSelectTop,
            canGoBack = canGoBack,
            onBack = onBack,
            focusRequesters = focusRequesters,
            downFocusRequester = mainDownFocusRequester,
            sidebarEntryRequester = sidebarEntryRequester,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp)
        )
        if (selectedArtistId != null) {
            TvArtistDetailScreen(
                artistId = selectedArtistId,
                currentTrack = currentTrack,
                isPlayerPlaying = isPlayerPlaying,
                isPlayerLoading = isPlayerLoading,
                entryFocusRequester = artistEntryFocusRequester,
                lastTrackFocusRequester = artistLastTrackFocusRequester,
                sidebarEntryRequester = sidebarEntryRequester,
                playerFocusRequester = playerFocusRequester,
                onPlayTrack = playTrackAndFocusPlayer,
                modifier = Modifier.weight(1f),
            )
        } else if (selectedTop == null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                TvDuetsHeroSlider(
                    items = duetItems,
                    isLoading = isHomeLoading,
                    focusRequester = duetFocusRequester,
                    topEntryRequester = focusRequesters.first(),
                    playerFocusRequester = programFocusRequester,
                    sidebarEntryRequester = sidebarEntryRequester,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 14.dp)
                )
                TvProgramsCarousel(
                    items = programItems,
                    isLoading = isHomeLoading,
                    firstCardFocusRequester = programFocusRequester,
                    duetFocusRequester = duetFocusRequester,
                    singerFocusRequester = singerFocusRequester,
                    sidebarEntryRequester = sidebarEntryRequester,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 28.dp)
                )
                TvFeaturedSingersCarousel(
                    items = singerItems,
                    isLoading = isHomeLoading,
                    firstCardFocusRequester = singerFocusRequester,
                    programFocusRequester = programFocusRequester,
                    modeFocusRequester = modeFocusRequester,
                    playerFocusRequester = modeFocusRequester,
                    sidebarEntryRequester = sidebarEntryRequester,
                    onOpenArtist = onOpenArtist,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 28.dp)
                )
                TvModesCarousel(
                    items = modeItems,
                    isLoading = isHomeLoading,
                    firstChipFocusRequester = modeFocusRequester,
                    singerFocusRequester = singerFocusRequester,
                    playerFocusRequester = musicianFocusRequester,
                    sidebarEntryRequester = sidebarEntryRequester,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 28.dp)
                )
                TvFeaturedMusiciansCarousel(
                    items = musicianItems,
                    isLoading = isHomeLoading,
                    firstCardFocusRequester = musicianFocusRequester,
                    modeFocusRequester = modeFocusRequester,
                    playerFocusRequester = trackRefreshFocusRequester,
                    sidebarEntryRequester = sidebarEntryRequester,
                    onOpenArtist = onOpenArtist,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 28.dp)
                )
                TvTopTracksSection(
                    tracks = topTrackItems,
                    isLoading = isHomeLoading,
                    isRefreshing = isTopTracksRefreshing,
                    onRefresh = { topTracksRefreshNonce += 1 },
                    refreshFocusRequester = trackRefreshFocusRequester,
                    firstRowFocusRequester = trackFocusRequester,
                    lastRowFocusRequester = topTracksLastFocusRequester,
                    musicianFocusRequester = musicianFocusRequester,
                    playerFocusRequester = playerFocusRequester,
                    sidebarEntryRequester = sidebarEntryRequester,
                    currentTrackId = currentTrack?.id,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    onPlayTrack = playTrackAndFocusPlayer,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 28.dp, bottom = 28.dp)
                )
            }
        } else {
            Spacer(Modifier.weight(1f))
        }
        TvBottomPlayer(
            focusRequester = playerFocusRequester,
            topEntryRequester = playerUpFocusRequester,
            sidebarEntryRequester = sidebarEntryRequester,
            currentTrack = currentTrack,
            isPlaying = isPlayerPlaying,
            isLoading = isPlayerLoading,
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            onTogglePlayback = onTogglePlayerPlayback,
        )
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun TvTopMenuBar(
    selectedTop: TvTopMenuItem?,
    onSelect: (TvTopMenuItem) -> Unit,
    canGoBack: Boolean,
    onBack: () -> Unit,
    focusRequesters: List<FocusRequester>,
    downFocusRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        TvTopMenuItem.entries.forEachIndexed { index, item ->
            val selected = selectedTop == item
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 11.25.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (selected) GolhaColors.PrimaryText else GolhaColors.SecondaryText,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .focusRequester(focusRequesters[index])
                    .focusProperties {
                        up = FocusRequester.Cancel
                        down = downFocusRequester
                        if (index == 0) {
                            right = sidebarEntryRequester
                        }
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) GolhaColors.BadgeBackground else Color.Transparent)
                    .border(
                        width = if (selected) 1.dp else 0.dp,
                        color = GolhaColors.Border,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelect(item) }
                    .focusable()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
            Spacer(Modifier.width(10.dp))
        }
        Spacer(Modifier.weight(1f))
        TvBackButton(
            enabled = canGoBack,
            onClick = onBack,
        )
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun TvDuetsHeroSlider(
    items: List<DuetPairUiModel>,
    isLoading: Boolean,
    focusRequester: FocusRequester,
    topEntryRequester: FocusRequester,
    playerFocusRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    var currentIndex by remember(items.size) { mutableStateOf(0) }
    val safeIndex = currentIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))

    LaunchedEffect(items.size) {
        if (items.size <= 1) return@LaunchedEffect
        while (true) {
            delay(4_000)
            currentIndex = (currentIndex + 1) % items.size
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(166.dp)
    ) {
        when {
            isLoading -> TvHomeLoadingCard(
                label = "در حال بارگذاری دوئت‌ها...",
                modifier = Modifier.tvMainFocusAnchor(
                    focusRequester = focusRequester,
                    upFocusRequester = topEntryRequester,
                    downFocusRequester = playerFocusRequester,
                    sidebarEntryRequester = sidebarEntryRequester,
                )
            )
            items.isEmpty() -> TvHomeEmptyCard(
                label = "دوئتی برای نمایش وجود ندارد",
                modifier = Modifier.tvMainFocusAnchor(
                    focusRequester = focusRequester,
                    upFocusRequester = topEntryRequester,
                    downFocusRequester = playerFocusRequester,
                    sidebarEntryRequester = sidebarEntryRequester,
                )
            )
            else -> {
                Crossfade(
                    targetState = items[safeIndex],
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                    label = "tvDuetSlider"
                ) { item ->
                    TvDuetCard(
                        item = item,
                        buttonFocusRequester = focusRequester,
                        topEntryRequester = topEntryRequester,
                        playerFocusRequester = playerFocusRequester,
                        sidebarEntryRequester = sidebarEntryRequester,
                    )
                }
            }
        }

        if (!isLoading && items.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (index == safeIndex) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == safeIndex) {
                                    GolhaColors.BannerDetail
                                } else {
                                    Color.White.copy(alpha = 0.35f)
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun TvDuetCard(
    item: DuetPairUiModel,
    buttonFocusRequester: FocusRequester,
    topEntryRequester: FocusRequester,
    playerFocusRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
) {
    val inf = rememberInfiniteTransition(label = "tvDuetCard")
    val glowAlpha by inf.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.10f,
        animationSpec = infiniteRepeatable(tween(2_000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "tvDuetGlow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF041334),
                        Color(0xFF08255B),
                        Color(0xFF031234)
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
            .drawBehind {
                drawCircle(
                    color = GolhaColors.BannerDetail.copy(alpha = glowAlpha),
                    radius = size.minDimension * 1.55f,
                    center = Offset(size.width * 0.82f, size.height * 0.45f)
                )
                drawCircle(
                    color = GolhaColors.BannerDetail.copy(alpha = glowAlpha * 0.8f),
                    radius = size.minDimension * 1.1f,
                    center = Offset(size.width * 0.08f, size.height * 1.0f)
                )
            }
            .padding(horizontal = 28.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${item.singer1} و ${item.singer2}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GolhaColors.BannerDetail,
                    maxLines = 1,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                if (item.trackCount > 0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${item.trackCount} ترک",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White.copy(alpha = 0.50f)
                    )
                }
                Spacer(Modifier.height(16.dp))
                TvDuetProgramsButton(
                    focusRequester = buttonFocusRequester,
                    topEntryRequester = topEntryRequester,
                    playerFocusRequester = playerFocusRequester,
                    sidebarEntryRequester = sidebarEntryRequester,
                )
            }

            Box(
                modifier = Modifier
                    .width(184.dp)
                    .height(120.dp)
            ) {
                TvDuetAvatar(
                    name = item.singer1,
                    imageUrl = item.singer1Avatar,
                    modifier = Modifier
                        .size(104.dp)
                        .align(Alignment.CenterStart)
                )
                TvDuetAvatar(
                    name = item.singer2,
                    imageUrl = item.singer2Avatar,
                    modifier = Modifier
                        .size(104.dp)
                        .align(Alignment.CenterEnd)
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun TvDuetProgramsButton(
    focusRequester: FocusRequester,
    topEntryRequester: FocusRequester,
    playerFocusRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
) {
    var isFocused by remember { mutableStateOf(false) }
    val background = if (isFocused) Color.White else GolhaColors.BannerDetail.copy(alpha = 0.92f)
    val foreground = Color(0xFF002045)

    Row(
        modifier = Modifier
            .focusRequester(focusRequester)
            .focusProperties {
                up = topEntryRequester
                down = playerFocusRequester
                right = sidebarEntryRequester
                left = FocusRequester.Cancel
            }
            .onFocusChanged { isFocused = it.isFocused }
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = GolhaColors.BannerDetail,
                shape = RoundedCornerShape(999.dp)
            )
            .clickable { }
            .focusable()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            tint = foreground,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "مشاهده برنامه‌ها",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            ),
            color = foreground
        )
    }
}

@Composable
private fun TvDuetAvatar(
    name: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = GolhaColors.BannerDetail.copy(alpha = 0.75f),
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun TvProgramsCarousel(
    items: List<ProgramUiModel>,
    isLoading: Boolean,
    firstCardFocusRequester: FocusRequester,
    duetFocusRequester: FocusRequester,
    singerFocusRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "برنامه‌ها",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            ),
            color = GolhaColors.PrimaryText,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))
        when {
            isLoading -> TvProgramLoadingRow(
                firstCardFocusRequester = firstCardFocusRequester,
                duetFocusRequester = duetFocusRequester,
                downFocusRequester = singerFocusRequester,
                sidebarEntryRequester = sidebarEntryRequester,
            )
            items.isEmpty() -> TvHomeEmptyCard(
                label = "برنامه‌ای برای نمایش وجود ندارد",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp)
                    .tvMainFocusAnchor(
                        focusRequester = firstCardFocusRequester,
                        upFocusRequester = duetFocusRequester,
                        downFocusRequester = singerFocusRequester,
                        sidebarEntryRequester = sidebarEntryRequester,
                    )
            )
            else -> {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    reverseLayout = false
                ) {
                    itemsIndexed(items) { index, item ->
                        TvProgramCard(
                            item = item,
                            modifier = Modifier
                                .then(if (index == 0) Modifier.focusRequester(firstCardFocusRequester) else Modifier)
                                .focusProperties {
                                    up = duetFocusRequester
                                    down = singerFocusRequester
                                    if (index == 0) {
                                        right = sidebarEntryRequester
                                    }
                                    if (index == items.lastIndex) {
                                        left = FocusRequester.Cancel
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun TvModesCarousel(
    items: List<DastgahUiModel>,
    isLoading: Boolean,
    firstChipFocusRequester: FocusRequester,
    singerFocusRequester: FocusRequester,
    playerFocusRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "دستگاه‌ها و آوازها",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            ),
            color = GolhaColors.PrimaryText,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(18.dp))
        when {
            isLoading -> TvModeLoadingRow(
                firstChipFocusRequester = firstChipFocusRequester,
                upFocusRequester = singerFocusRequester,
                downFocusRequester = playerFocusRequester,
                sidebarEntryRequester = sidebarEntryRequester,
            )
            items.isEmpty() -> TvHomeEmptyCard(
                label = "دستگاهی برای نمایش وجود ندارد",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .tvMainFocusAnchor(
                        focusRequester = firstChipFocusRequester,
                        upFocusRequester = singerFocusRequester,
                        downFocusRequester = playerFocusRequester,
                        sidebarEntryRequester = sidebarEntryRequester,
                    )
            )
            else -> {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    reverseLayout = false
                ) {
                    itemsIndexed(items) { index, mode ->
                        TvModeChip(
                            title = mode.name,
                            modifier = Modifier
                                .then(if (index == 0) Modifier.focusRequester(firstChipFocusRequester) else Modifier)
                                .focusProperties {
                                    up = singerFocusRequester
                                    down = playerFocusRequester
                                    if (index == 0) {
                                        right = sidebarEntryRequester
                                    }
                                    if (index == items.lastIndex) {
                                        left = FocusRequester.Cancel
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun TvFeaturedSingersCarousel(
    items: List<SingerUiModel>,
    isLoading: Boolean,
    firstCardFocusRequester: FocusRequester,
    programFocusRequester: FocusRequester,
    modeFocusRequester: FocusRequester,
    playerFocusRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
    onOpenArtist: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "خوانندگان برجسته",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            ),
            color = GolhaColors.PrimaryText,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(18.dp))
        when {
            isLoading -> TvArtistLoadingRow(
                firstCardFocusRequester = firstCardFocusRequester,
                upFocusRequester = programFocusRequester,
                downFocusRequester = playerFocusRequester,
                sidebarEntryRequester = sidebarEntryRequester,
            )
            items.isEmpty() -> TvHomeEmptyCard(
                label = "خواننده‌ای برای نمایش وجود ندارد",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(225.dp)
                    .tvMainFocusAnchor(
                        focusRequester = firstCardFocusRequester,
                        upFocusRequester = programFocusRequester,
                        downFocusRequester = playerFocusRequester,
                        sidebarEntryRequester = sidebarEntryRequester,
                    )
            )
            else -> {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    reverseLayout = false
                ) {
                    itemsIndexed(items) { index, singer ->
                        TvArtistCard(
                            item = TvArtistCardItem(
                                id = singer.id,
                                name = singer.name,
                                role = "${singer.programCount} ترک",
                                imageUrl = singer.imageUrl,
                            ),
                            modifier = Modifier
                                .then(if (index == 0) Modifier.focusRequester(firstCardFocusRequester) else Modifier)
                                .focusProperties {
                                    up = programFocusRequester
                                    down = playerFocusRequester
                                    if (index == 0) {
                                        right = sidebarEntryRequester
                                    }
                                    if (index == items.lastIndex) {
                                        left = FocusRequester.Cancel
                                    }
                                },
                            onClick = { onOpenArtist(singer.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun TvFeaturedMusiciansCarousel(
    items: List<MusicianUiModel>,
    isLoading: Boolean,
    firstCardFocusRequester: FocusRequester,
    modeFocusRequester: FocusRequester,
    playerFocusRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
    onOpenArtist: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "نوازندگان برجسته",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            ),
            color = GolhaColors.PrimaryText,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(18.dp))
        when {
            isLoading -> TvArtistLoadingRow(
                firstCardFocusRequester = firstCardFocusRequester,
                upFocusRequester = modeFocusRequester,
                downFocusRequester = playerFocusRequester,
                sidebarEntryRequester = sidebarEntryRequester,
            )
            items.isEmpty() -> TvHomeEmptyCard(
                label = "نوازنده‌ای برای نمایش وجود ندارد",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(225.dp)
                    .tvMainFocusAnchor(
                        focusRequester = firstCardFocusRequester,
                        upFocusRequester = modeFocusRequester,
                        downFocusRequester = playerFocusRequester,
                        sidebarEntryRequester = sidebarEntryRequester,
                    )
            )
            else -> {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    reverseLayout = false
                ) {
                    itemsIndexed(items) { index, musician ->
                        TvArtistCard(
                            item = TvArtistCardItem(
                                id = musician.id,
                                name = musician.name,
                                role = musician.instrument.ifBlank { "${musician.programCount} ترک" },
                                imageUrl = musician.imageUrl,
                            ),
                            modifier = Modifier
                                .then(if (index == 0) Modifier.focusRequester(firstCardFocusRequester) else Modifier)
                                .focusProperties {
                                    up = modeFocusRequester
                                    down = playerFocusRequester
                                    if (index == 0) {
                                        right = sidebarEntryRequester
                                    }
                                    if (index == items.lastIndex) {
                                        left = FocusRequester.Cancel
                                    }
                                },
                            onClick = { onOpenArtist(musician.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun TvTopTracksSection(
    tracks: List<TrackUiModel>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    refreshFocusRequester: FocusRequester,
    firstRowFocusRequester: FocusRequester,
    lastRowFocusRequester: FocusRequester,
    musicianFocusRequester: FocusRequester,
    playerFocusRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
    currentTrackId: Long?,
    isPlayerPlaying: Boolean,
    isPlayerLoading: Boolean,
    onPlayTrack: (TrackUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayedTracks = remember(tracks) { tracks.take(5) }
    val rowFocusRequesters = remember(displayedTracks.size) {
        List(displayedTracks.size) { FocusRequester() }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TvRefreshButton(
                    isRefreshing = isRefreshing,
                    onClick = onRefresh,
                    modifier = Modifier
                        .focusRequester(refreshFocusRequester)
                        .focusProperties {
                            up = musicianFocusRequester
                            down = firstRowFocusRequester
                            right = sidebarEntryRequester
                            left = FocusRequester.Cancel
                        },
                )
                Text(
                    text = "برترین ترک‌ها",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GolhaColors.PrimaryText,
                    textAlign = TextAlign.End,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        when {
            isLoading -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(5) { index ->
                        TvTrackLoadingRow(
                            modifier = Modifier
                                .then(
                                    if (index == 0) {
                                        Modifier.tvMainFocusAnchor(
                                            focusRequester = firstRowFocusRequester,
                                            upFocusRequester = refreshFocusRequester,
                                            downFocusRequester = playerFocusRequester,
                                            sidebarEntryRequester = sidebarEntryRequester,
                                        )
                                    } else {
                                        Modifier.focusProperties {
                                            up = if (index == 1) firstRowFocusRequester else FocusRequester.Cancel
                                            down = if (index == 4) playerFocusRequester else FocusRequester.Cancel
                                        }
                                    }
                                )
                        )
                    }
                }
            }
            displayedTracks.isEmpty() -> TvHomeEmptyCard(
                label = "ترکی برای نمایش وجود ندارد",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .tvMainFocusAnchor(
                        focusRequester = firstRowFocusRequester,
                        upFocusRequester = refreshFocusRequester,
                        downFocusRequester = playerFocusRequester,
                        sidebarEntryRequester = sidebarEntryRequester,
                    )
            )
            else -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    displayedTracks.forEachIndexed { index, track ->
                        val currentFocusRequester = when {
                            index == 0 -> firstRowFocusRequester
                            index == displayedTracks.lastIndex -> lastRowFocusRequester
                            else -> rowFocusRequesters[index]
                        }
                        val previousFocusRequester = when (index) {
                            0 -> refreshFocusRequester
                            1 -> firstRowFocusRequester
                            else -> rowFocusRequesters[index - 1]
                        }
                        val nextFocusRequester = when {
                            index == displayedTracks.lastIndex -> playerFocusRequester
                            index + 1 == displayedTracks.lastIndex -> lastRowFocusRequester
                            else -> rowFocusRequesters[index + 1]
                        }

                        TvTrackRow(
                            item = TvTrackRowItem(
                                id = track.id,
                                title = track.title,
                                artist = track.artist,
                                duration = track.duration,
                                coverUrl = track.coverUrl,
                            ),
                            modifier = Modifier
                                .focusRequester(currentFocusRequester)
                                .then(
                                    if (index == 0 && displayedTracks.size == 1) {
                                        Modifier.focusRequester(lastRowFocusRequester)
                                    } else {
                                        Modifier
                                    }
                                )
                                .focusProperties {
                                    up = previousFocusRequester
                                    down = nextFocusRequester
                                    if (index == 0) {
                                        right = sidebarEntryRequester
                                    }
                                    left = FocusRequester.Cancel
                                },
                            isPlaying = isPlayerPlaying && currentTrackId == track.id,
                            isLoading = isPlayerLoading && currentTrackId == track.id,
                            onClick = { onPlayTrack(track) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvRefreshButton(
    isRefreshing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (isFocused) GolhaColors.BannerDetail else GolhaColors.Surface.copy(alpha = 0.86f))
            .border(
                width = 1.dp,
                color = if (isFocused) GolhaColors.BannerDetail else GolhaColors.Border.copy(alpha = 0.6f),
                shape = CircleShape,
            )
            .clickable(enabled = !isRefreshing) { onClick() }
            .onFocusChanged { isFocused = it.isFocused }
            .focusable(),
        contentAlignment = Alignment.Center,
    ) {
        if (isRefreshing) {
            CircularProgressIndicator(
                color = if (isFocused) Color.White else GolhaColors.BannerDetail,
                strokeWidth = 2.dp,
                modifier = Modifier.size(17.dp),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                tint = if (isFocused) Color.White else GolhaColors.PrimaryText.copy(alpha = 0.72f),
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
private fun TvHomeLoadingCard(
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF041334),
                        Color(0xFF08255B),
                        Color(0xFF031234)
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                color = GolhaColors.BannerDetail,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White.copy(alpha = 0.86f)
            )
        }
    }
}

@Composable
private fun TvHomeEmptyCard(
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(GolhaColors.Surface.copy(alpha = 0.9f))
            .border(1.dp, GolhaColors.Border.copy(alpha = 0.75f), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            ),
            color = GolhaColors.SecondaryText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun TvModeChip(
    title: String,
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 10.5.sp,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium
        ),
        color = GolhaColors.PrimaryText,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .clip(RoundedCornerShape(999.dp))
            .background(if (isFocused) Color.White else GolhaColors.Surface.copy(alpha = 0.92f))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) GolhaColors.BannerDetail else GolhaColors.Border,
                shape = RoundedCornerShape(999.dp)
            )
            .clickable { }
            .focusable()
            .padding(horizontal = 32.dp, vertical = 12.dp)
    )
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun TvModeLoadingRow(
    firstChipFocusRequester: FocusRequester,
    upFocusRequester: FocusRequester,
    downFocusRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        reverseLayout = false
    ) {
        items(6) { index ->
            Box(
                modifier = Modifier
                    .then(
                        if (index == 0) {
                            Modifier.tvMainFocusAnchor(
                                focusRequester = firstChipFocusRequester,
                                upFocusRequester = upFocusRequester,
                                downFocusRequester = downFocusRequester,
                                sidebarEntryRequester = sidebarEntryRequester,
                            )
                        } else {
                            Modifier.focusProperties {
                                up = upFocusRequester
                                down = downFocusRequester
                                if (index == 5) {
                                    left = FocusRequester.Cancel
                                }
                            }
                        }
                    )
                    .width(if (index % 2 == 0) 112.dp else 92.dp)
                    .height(45.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(GolhaColors.Surface.copy(alpha = 0.92f))
                    .border(1.dp, GolhaColors.Border, RoundedCornerShape(999.dp))
                    .focusable()
            )
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun TvArtistLoadingRow(
    firstCardFocusRequester: FocusRequester,
    upFocusRequester: FocusRequester,
    downFocusRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        reverseLayout = false
    ) {
        items(4) { index ->
            Column(
                modifier = Modifier
                    .then(
                        if (index == 0) {
                            Modifier.tvMainFocusAnchor(
                                focusRequester = firstCardFocusRequester,
                                upFocusRequester = upFocusRequester,
                                downFocusRequester = downFocusRequester,
                                sidebarEntryRequester = sidebarEntryRequester,
                            )
                        } else {
                            Modifier.focusProperties {
                                up = upFocusRequester
                                down = downFocusRequester
                                if (index == 3) {
                                    left = FocusRequester.Cancel
                                }
                            }
                        }
                    )
                    .width(156.dp)
                    .height(225.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0x1AC4C6CF), RoundedCornerShape(12.dp))
                    .focusable()
            ) {
                Box(
                    modifier = Modifier
                        .size(156.dp)
                        .background(Color(0xFFE5E2DA))
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(69.dp)
                        .padding(horizontal = 15.dp, vertical = 13.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(GolhaColors.PrimaryText.copy(alpha = 0.08f))
                    )
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.46f)
                            .height(7.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(GolhaColors.PrimaryText.copy(alpha = 0.06f))
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun TvProgramLoadingRow(
    firstCardFocusRequester: FocusRequester,
    duetFocusRequester: FocusRequester,
    downFocusRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        reverseLayout = false
    ) {
        items(4) { index ->
            Row(
                modifier = Modifier
                    .then(
                        if (index == 0) {
                            Modifier.tvMainFocusAnchor(
                                focusRequester = firstCardFocusRequester,
                                upFocusRequester = duetFocusRequester,
                                downFocusRequester = downFocusRequester,
                                sidebarEntryRequester = sidebarEntryRequester,
                            )
                        } else {
                            Modifier.focusProperties {
                                up = duetFocusRequester
                                down = downFocusRequester
                                if (index == 3) {
                                    left = FocusRequester.Cancel
                                }
                            }
                        }
                    )
                    .width(172.dp)
                    .height(66.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GolhaColors.Surface.copy(alpha = 0.9f))
                    .border(1.dp, GolhaColors.Border.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
                    .padding(9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GolhaColors.PrimaryText.copy(alpha = 0.06f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = GolhaColors.BannerDetail,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (index % 2 == 0) 0.78f else 0.58f)
                            .height(9.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(GolhaColors.PrimaryText.copy(alpha = 0.08f))
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.42f)
                            .height(7.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(GolhaColors.PrimaryText.copy(alpha = 0.06f))
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.tvMainFocusAnchor(
    focusRequester: FocusRequester,
    upFocusRequester: FocusRequester,
    downFocusRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
): Modifier = this
    .focusRequester(focusRequester)
    .focusProperties {
        up = upFocusRequester
        down = downFocusRequester
        right = sidebarEntryRequester
        left = FocusRequester.Cancel
    }
    .focusable()

@Composable
private fun TvProgramCard(
    item: ProgramUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .width(172.dp)
            .height(66.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GolhaColors.Surface.copy(alpha = 0.9f))
            .border(1.dp, GolhaColors.Border.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
            .clickable { }
            .focusable()
            .padding(9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GolhaColors.PrimaryText.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            TvProgramGlyph(title = item.title)
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.title.replace("\n", " "),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = GolhaColors.PrimaryText,
                maxLines = 1,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${item.episodeCount} برنامه",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 7.5.sp),
                color = Color(0xFF7D786D),
                maxLines = 1,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TvProgramGlyph(title: String) {
    val kind = remember(title) {
        when {
            title.contains("رنگارنگ") -> 0
            title.contains("تازه") -> 1
            title.contains("یک شاخه") -> 2
            title.contains("سبز") -> 3
            else -> 4
        }
    }
    Canvas(modifier = Modifier.size(18.dp)) {
        val gold = GolhaColors.BannerDetail
        val stroke = 2.4.dp.toPx()
        when (kind) {
            0 -> {
                val path = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.06f)
                    lineTo(size.width * 0.86f, size.height * 0.28f)
                    lineTo(size.width * 0.86f, size.height * 0.72f)
                    lineTo(size.width * 0.5f, size.height * 0.94f)
                    lineTo(size.width * 0.14f, size.height * 0.72f)
                    lineTo(size.width * 0.14f, size.height * 0.28f)
                    close()
                }
                drawPath(path, color = gold.copy(alpha = 0.95f), style = Stroke(width = stroke, cap = StrokeCap.Round))
            }
            1 -> {
                drawCircle(gold.copy(alpha = 0.95f), radius = size.minDimension * 0.34f, style = Stroke(width = stroke))
                drawCircle(gold.copy(alpha = 0.95f), radius = size.minDimension * 0.17f, style = Stroke(width = stroke))
            }
            2 -> {
                drawLine(gold, Offset(size.width * 0.5f, size.height * 0.08f), Offset(size.width * 0.5f, size.height * 0.92f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(gold, Offset(size.width * 0.08f, size.height * 0.5f), Offset(size.width * 0.92f, size.height * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawCircle(gold, radius = 3.dp.toPx(), center = Offset(size.width * 0.5f, size.height * 0.5f))
            }
            3 -> {
                val path = Path().apply {
                    moveTo(size.width * 0.18f, size.height * 0.45f)
                    cubicTo(size.width * 0.38f, size.height * 0.06f, size.width * 0.86f, size.height * 0.16f, size.width * 0.82f, size.height * 0.68f)
                    cubicTo(size.width * 0.48f, size.height * 0.78f, size.width * 0.24f, size.height * 0.72f, size.width * 0.18f, size.height * 0.45f)
                }
                drawPath(path, color = gold, style = Stroke(width = stroke, cap = StrokeCap.Round))
                drawLine(gold.copy(alpha = 0.8f), Offset(size.width * 0.28f, size.height * 0.58f), Offset(size.width * 0.72f, size.height * 0.62f), strokeWidth = stroke * 0.65f, cap = StrokeCap.Round)
            }
            else -> {
                drawCircle(gold, radius = size.minDimension * 0.36f, style = Stroke(width = stroke))
            }
        }
    }
}

@Composable
private fun TvBackButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(GolhaColors.Surface.copy(alpha = 0.85f))
            .clickable(enabled = enabled) { onClick() }
            .focusable(enabled = enabled),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(14.dp)) {
            val color = GolhaColors.PrimaryText.copy(alpha = if (enabled) 0.72f else 0.28f)
            val strokeWidth = 2.2.dp.toPx()
            drawLine(
                color = color,
                start = Offset(size.width * 0.65f, size.height * 0.12f),
                end = Offset(size.width * 0.28f, size.height * 0.5f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(size.width * 0.28f, size.height * 0.5f),
                end = Offset(size.width * 0.65f, size.height * 0.88f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun TvBottomPlayer(
    focusRequester: FocusRequester,
    topEntryRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
    currentTrack: TrackUiModel?,
    isPlaying: Boolean,
    isLoading: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onTogglePlayback: () -> Unit,
) {
    val progress = remember(currentTrack?.id, currentPositionMs, durationMs) {
        if (durationMs > 0L) currentPositionMs.toFloat() / durationMs.toFloat() else 0f
    }
    val hasPlayableTrack = !currentTrack?.audioUrl.isNullOrBlank()
    val playButtonInteractionSource = remember { MutableInteractionSource() }
    val isPlayButtonFocused by playButtonInteractionSource.collectIsFocusedAsState()
    val playButtonFocusedColor = Color(0xFFFFC93A)
    val playButtonIdleColor = Color.White
    val playButtonBackground = if (isPlayButtonFocused) playButtonFocusedColor else playButtonIdleColor

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(Color(0xFF002045))
        ) {
            TvPlayerSeekBar(progress = progress)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TvEqualizerIndicator(isActive = isPlaying)
                    Text(
                        text = "${formatTvPlaybackTime(currentPositionMs)} / ${formatTvPlaybackTime(durationMs)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(Color.White.copy(alpha = 0.1f))
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(260.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    TvPlayerCanvasIcon(TvPlayerIconKind.Shuffle, alpha = 0.35f)
                    Spacer(Modifier.width(18.dp))
                    TvPlayerCanvasIcon(TvPlayerIconKind.BackTen, alpha = 0.35f)
                    Spacer(Modifier.width(18.dp))
                    Box(
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .focusProperties {
                                up = topEntryRequester
                                down = FocusRequester.Cancel
                                right = sidebarEntryRequester
                            }
                            .size(42.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(playButtonBackground)
                            .border(
                                width = if (isPlayButtonFocused) 3.dp else 0.dp,
                                color = if (isPlayButtonFocused) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(11.dp),
                            )
                            .clickable(
                                enabled = hasPlayableTrack,
                                interactionSource = playButtonInteractionSource,
                                indication = null,
                            ) { onTogglePlayback() }
                            .focusable(interactionSource = playButtonInteractionSource),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isLoading -> CircularProgressIndicator(
                                color = Color(0xFF002045),
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp),
                            )
                            isPlaying -> TvPlayerPauseGlyph(
                                color = Color(0xFF002045),
                                modifier = Modifier.size(18.dp),
                            )
                            else -> Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF002045),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(18.dp))
                    TvPlayerCanvasIcon(TvPlayerIconKind.ForwardTen, alpha = 0.35f)
                    Spacer(Modifier.width(18.dp))
                    TvPlayerCanvasIcon(TvPlayerIconKind.Repeat, alpha = 0.35f)
                }

                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.width(220.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = currentTrack?.title ?: "ترکی انتخاب نشده",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            maxLines = 1
                        )
                        Text(
                            text = currentTrack?.artist ?: "برای شروع، یک ترک را پخش کنید",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            maxLines = 1
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val cover = currentTrack?.coverUrl ?: currentTrack?.artistImages?.firstOrNull()
                        if (!cover.isNullOrBlank()) {
                            AsyncImage(
                                model = cover,
                                contentDescription = currentTrack?.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            Text(
                                text = "♪",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvPlayerPauseGlyph(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
        Spacer(Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
    }
}

private fun formatTvPlaybackTime(timeMs: Long): String {
    val totalSeconds = (timeMs / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    val minuteText = minutes.toString().padStart(2, '0')
    val secondText = seconds.toString().padStart(2, '0')
    return if (hours > 0L) "$hours:$minuteText:$secondText" else "$minuteText:$secondText"
}

@Composable
private fun TvPlayerSeekBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(Color.White.copy(alpha = 0.1f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(4.dp)
                .background(Color(0xFFD4AF37))
        )
    }
}

@Composable
private fun TvEqualizerIndicator(isActive: Boolean) {
    val heights = listOf(6.dp, 15.dp, 24.dp, 12.dp, 18.dp, 9.dp)
    Row(
        modifier = Modifier
            .width(36.dp)
            .height(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        heights.forEach { height ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isActive) {
                            Color(0xFFD4AF37)
                        } else {
                            GolhaColors.SecondaryText.copy(alpha = 0.35f)
                        }
                    )
            )
        }
    }
}

private enum class TvPlayerIconKind {
    Shuffle,
    BackTen,
    ForwardTen,
    Repeat,
}

@Composable
private fun TvPlayerCanvasIcon(
    kind: TvPlayerIconKind,
    alpha: Float,
) {
    val tint = Color.White.copy(alpha = alpha)
    Box(
        modifier = Modifier.size(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 2.2.dp.toPx()
            val w = size.width
            val h = size.height

            when (kind) {
                TvPlayerIconKind.Shuffle -> {
                    drawLine(tint, Offset(w * 0.16f, h * 0.32f), Offset(w * 0.42f, h * 0.32f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(tint, Offset(w * 0.42f, h * 0.32f), Offset(w * 0.74f, h * 0.68f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(tint, Offset(w * 0.16f, h * 0.68f), Offset(w * 0.42f, h * 0.68f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(tint, Offset(w * 0.42f, h * 0.68f), Offset(w * 0.74f, h * 0.32f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawSmallArrow(tint, Offset(w * 0.82f, h * 0.68f), forward = true, strokeWidth = strokeWidth)
                    drawSmallArrow(tint, Offset(w * 0.82f, h * 0.32f), forward = true, strokeWidth = strokeWidth)
                }
                TvPlayerIconKind.BackTen -> {
                    drawLine(tint, Offset(w * 0.28f, h * 0.24f), Offset(w * 0.28f, h * 0.76f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawTriangle(
                        tint,
                        Offset(w * 0.72f, h * 0.22f),
                        Offset(w * 0.40f, h * 0.50f),
                        Offset(w * 0.72f, h * 0.78f)
                    )
                }
                TvPlayerIconKind.ForwardTen -> {
                    drawTriangle(
                        tint,
                        Offset(w * 0.28f, h * 0.22f),
                        Offset(w * 0.60f, h * 0.50f),
                        Offset(w * 0.28f, h * 0.78f)
                    )
                    drawLine(tint, Offset(w * 0.72f, h * 0.24f), Offset(w * 0.72f, h * 0.76f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                }
                TvPlayerIconKind.Repeat -> {
                    drawLine(tint, Offset(w * 0.18f, h * 0.36f), Offset(w * 0.78f, h * 0.36f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawSmallArrow(tint, Offset(w * 0.82f, h * 0.36f), forward = true, strokeWidth = strokeWidth)
                    drawLine(tint, Offset(w * 0.82f, h * 0.64f), Offset(w * 0.22f, h * 0.64f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawSmallArrow(tint, Offset(w * 0.18f, h * 0.64f), forward = false, strokeWidth = strokeWidth)
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSmallArrow(
    color: Color,
    tip: Offset,
    forward: Boolean,
    strokeWidth: Float,
) {
    val x = if (forward) -1f else 1f
    drawLine(color, tip, Offset(tip.x + x * 4.dp.toPx(), tip.y - 3.dp.toPx()), strokeWidth = strokeWidth, cap = StrokeCap.Round)
    drawLine(color, tip, Offset(tip.x + x * 4.dp.toPx(), tip.y + 3.dp.toPx()), strokeWidth = strokeWidth, cap = StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTriangle(
    color: Color,
    first: Offset,
    second: Offset,
    third: Offset,
) {
    drawPath(
        path = Path().apply {
            moveTo(first.x, first.y)
            lineTo(second.x, second.y)
            lineTo(third.x, third.y)
            close()
        },
        color = color
    )
}

@Composable
private fun TvSidebar(
    selectedItem: TvSideMenuItem,
    onSelectItem: (TvSideMenuItem) -> Unit,
    focusRequesters: List<FocusRequester>,
    mainEntryRequester: FocusRequester,
) {
    Column(
        modifier = Modifier
            .width(210.dp)
            .fillMaxHeight()
            .background(Color(0xFFF1EEE5))
            .padding(horizontal = 16.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "رادیو گل‌ها",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = GolhaColors.PrimaryText,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "میراث موسیقی اصیل ایران",
            style = MaterialTheme.typography.bodySmall,
            color = GolhaColors.SecondaryText,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Spacer(Modifier.height(24.dp))

        TvSideMenuItem.entries.forEachIndexed { index, item ->
            if (item == TvSideMenuItem.Settings) {
                Spacer(modifier = Modifier.weight(1f))
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0x1A1C1C17))
                )
                Spacer(Modifier.height(12.dp))
            }

            TvSidebarItem(
                item = item,
                selected = item == selectedItem,
                onClick = { onSelectItem(item) },
                focusRequester = focusRequesters[index],
                upFocusRequester = focusRequesters.getOrNull(index - 1),
                downFocusRequester = focusRequesters.getOrNull(index + 1),
                mainEntryRequester = mainEntryRequester,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun TvSidebarItem(
    item: TvSideMenuItem,
    selected: Boolean,
    onClick: () -> Unit,
    focusRequester: FocusRequester,
    upFocusRequester: FocusRequester?,
    downFocusRequester: FocusRequester?,
    mainEntryRequester: FocusRequester,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .focusProperties {
                left = mainEntryRequester
                right = FocusRequester.Cancel
                up = upFocusRequester ?: FocusRequester.Cancel
                down = downFocusRequester ?: FocusRequester.Cancel
            }
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFFE8E2D1) else Color.Transparent)
            .clickable { onClick() }
            .focusable()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = if (selected) GolhaColors.PrimaryText else Color(0xFF555555),
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 11.25.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (selected) GolhaColors.PrimaryText else Color(0xFF333333),
            textAlign = TextAlign.Start
        )
    }
    Spacer(Modifier.height(6.dp))
}
