package com.radiogolha.mobile.ui.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.*
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.foundation.pager.*
import com.radiogolha.mobile.theme.*
import com.radiogolha.mobile.ui.home.*
import com.radiogolha.mobile.ui.programs.ProgramTrackRow
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    favoriteSingers: List<SingerListItemUiModel> = emptyList(),
    favoriteMusicians: List<MusicianListItemUiModel> = emptyList(),
    onArtistClick: (Long) -> Unit = {},
    onShowAllFavorites: () -> Unit = {},
    onOpenDebug: () -> Unit = {},
    isUpdatingDatabaseFromCdn: Boolean = false,
    databaseUpdateProgress: Float? = null,
    onUpdateDatabaseFromCdn: () -> Unit = {},
    isDebugDatabaseToolsEnabled: Boolean,
    isImportingDatabase: Boolean,
    onImportDebugDatabase: () -> Unit,
    currentTrack: TrackUiModel? = null,
    isPlayerPlaying: Boolean = false,
    isPlayerLoading: Boolean = false,
    currentPlaybackPositionMs: Long = 0L,
    currentPlaybackDurationMs: Long = 0L,
    onTogglePlayerPlayback: () -> Unit = {},
    onExpandPlayer: () -> Unit = {},
    mostPlayedTracks: List<TrackUiModel> = emptyList(),
    recentlyPlayedTracks: List<TrackUiModel> = emptyList(),
    savedPlaylists: List<SavedPlaylistUiModel> = emptyList(),
    initialTabIndex: Int = 0,
    onTrackClick: (Long) -> Unit = {},
    onPlayTrack: (TrackUiModel) -> Unit = {},
    onTrackLongClick: (TrackUiModel) -> Unit = {},
    onPlaylistClick: (Long) -> Unit = {},
    onPlaylistLongClick: (Long) -> Unit = {},
) {
    var aboutTapCount by rememberSaveable { mutableIntStateOf(0) }
    var isDebugToolsVisible by rememberSaveable { mutableStateOf(false) }
    val tabs = listOf("علاقه‌مندی‌ها", "لیست‌های من", "پخش اخیر", "محبوب‌ترین‌ها", "درباره")
    
    val pagerState = rememberPagerState(
        initialPage = initialTabIndex.coerceIn(0, tabs.size - 1),
        pageCount = { tabs.size }
    )
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Rtl) {
        GolhaPatternBackground {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                bottomBar = {
                    BottomNavigationWithMiniPlayer(
                        items = bottomNavItems,
                        onItemSelected = onBottomNavSelected,
                        currentTrack = currentTrack,
                        isPlaying = isPlayerPlaying,
                        isLoading = isPlayerLoading,
                        currentPositionMs = currentPlaybackPositionMs,
                        durationMs = currentPlaybackDurationMs,
                        onTogglePlayback = onTogglePlayerPlayback,
                        onExpand = onExpandPlayer,
                    )
                },
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    // Title
                    Text(
                        text = "حساب من",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = GolhaColors.PrimaryText,
                        modifier = Modifier.padding(horizontal = GolhaSpacing.ScreenHorizontal, vertical = 20.dp)
                    )

                    // Tabs (Matching LibraryScreen)
                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = Color.Transparent,
                        contentColor = GolhaColors.PrimaryAccent,
                        edgePadding = GolhaSpacing.ScreenHorizontal,
                        divider = {},
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                color = GolhaColors.PrimaryAccent
                            )
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val selected = pagerState.currentPage == index
                            Tab(
                                selected = selected,
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 17.sp
                                        ),
                                        color = if (selected) GolhaColors.PrimaryText else GolhaColors.SecondaryText
                                    )
                                }
                            )
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
                    ) { page ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = GolhaSpacing.ScreenHorizontal,
                                end = GolhaSpacing.ScreenHorizontal,
                                top = 12.dp,
                                bottom = 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            when (page) {
                                0 -> { // Favorites
                                    if (favoriteSingers.isEmpty() && favoriteMusicians.isEmpty()) {
                                        item { EmptyStatePlaceholder("هنوز هنرمندی را به علاقه‌مندی‌ها اضافه نکرده‌اید.") }
                                    }
                                    
                                    if (favoriteSingers.isNotEmpty()) {
                                        item { Text("خوانندگان مورد علاقه", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText) }
                                        items(favoriteSingers) { ArtistFavoriteItem(it, onArtistClick) }
                                    }

                                    if (favoriteMusicians.isNotEmpty()) {
                                        item { Text("نوازندگان مورد علاقه", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText, modifier = Modifier.padding(top = 8.dp)) }
                                        items(favoriteMusicians) { MusicianFavoriteItem(it, onArtistClick) }
                                    }
                                }
                                1 -> { // Playlists
                                    if (savedPlaylists.isEmpty()) {
                                        item { EmptyStatePlaceholder("هنوز لیست پخشی نساخته‌اید.") }
                                    } else {
                                        items(savedPlaylists) { PlaylistAccountItem(it, onPlaylistClick, onPlaylistLongClick) }
                                    }
                                }
                                2 -> { // Recent
                                    if (recentlyPlayedTracks.isEmpty()) {
                                        item { EmptyStatePlaceholder("تاریخچه پخش شما خالی است.") }
                                    } else {
                                        item { TrackListContainer(recentlyPlayedTracks.take(10), currentTrack, isPlayerPlaying, onTrackClick, onPlayTrack, onTrackLongClick, onArtistClick) }
                                    }
                                }
                                3 -> { // Popular
                                    if (mostPlayedTracks.isEmpty()) {
                                        item { EmptyStatePlaceholder("هنوز آهنگ محبوب خاصی ندارید.") }
                                    } else {
                                        item { TrackListContainer(mostPlayedTracks.take(10), currentTrack, isPlayerPlaying, onTrackClick, onPlayTrack, onTrackLongClick, onArtistClick) }
                                    }
                                }
                                4 -> { // About
                                    item { 
                                        AboutAppSection(onTap = {
                                            aboutTapCount++
                                            if (aboutTapCount == 2) {
                                                // show debug indicator potentially
                                            } else if (aboutTapCount >= 3) {
                                                isDebugToolsVisible = true
                                            }
                                        }) 
                                    }

                                    item {
                                        DatabaseUpdateSection(
                                            isUpdating = isUpdatingDatabaseFromCdn,
                                            progress = databaseUpdateProgress,
                                            onUpdateClick = onUpdateDatabaseFromCdn,
                                        )
                                    }
                                    
                                    if (isDebugToolsVisible) {
                                        item {
                                            AnimatedVisibility(
                                                visible = isDebugToolsVisible && isDebugDatabaseToolsEnabled,
                                                enter = expandVertically() + fadeIn(),
                                                exit = shrinkVertically() + fadeOut()
                                            ) {
                                                DebugSection(
                                                    isDebugDatabaseToolsEnabled = isDebugDatabaseToolsEnabled,
                                                    isImportingDatabase = isImportingDatabase,
                                                    onImportDebugDatabase = onImportDebugDatabase
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DatabaseUpdateSection(
    isUpdating: Boolean,
    progress: Float?,
    onUpdateClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.padding(top = 8.dp),
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        tonalElevation = 0.dp,
        shadowElevation = GolhaElevation.Card,
        border = BorderStroke(1.dp, GolhaColors.Border),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "به‌روزرسانی دیتابیس",
                style = MaterialTheme.typography.titleLarge,
                color = GolhaColors.PrimaryText,
            )
            SmallPrimaryButton(
                label = "دریافت نسخه جدید",
                enabled = !isUpdating,
                loading = isUpdating,
                onClick = onUpdateClick,
            )

            if (isUpdating) {
                val value = progress?.coerceIn(0f, 1f)
                if (value != null) {
                    LinearProgressIndicator(
                        progress = { value },
                        modifier = Modifier.fillMaxWidth(),
                        color = GolhaColors.PrimaryAccent,
                        trackColor = GolhaColors.Border.copy(alpha = 0.45f),
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = GolhaColors.PrimaryAccent,
                        trackColor = GolhaColors.Border.copy(alpha = 0.45f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackListContainer(
    tracks: List<TrackUiModel>,
    currentTrack: TrackUiModel?,
    isPlayerPlaying: Boolean,
    onTrackClick: (Long) -> Unit,
    onPlayTrack: (TrackUiModel) -> Unit,
    onTrackLongClick: (TrackUiModel) -> Unit,
    onArtistClick: (Long) -> Unit,
) {
    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GolhaColors.Surface, RoundedCornerShape(GolhaRadius.Card))
                .border(1.dp, GolhaColors.Border.copy(alpha = 0.6f), RoundedCornerShape(GolhaRadius.Card))
                .padding(vertical = 8.dp),
        ) {
            tracks.forEachIndexed { index, track ->
                val isActive = currentTrack?.id == track.id
                ProgramTrackRow(
                    track = track,
                    onTrackClick = { onTrackClick(track.id) },
                    onPlayClick = { onPlayTrack(track) },
                    onLongClick = { onTrackLongClick(track) },
                    onArtistClick = { id -> onArtistClick(id) },
                    isActive = isActive,
                    isPlaying = isActive && isPlayerPlaying,
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
private fun EmptyStatePlaceholder(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GolhaLineIcon(icon = GolhaIcon.Note, modifier = Modifier.size(64.dp), tint = GolhaColors.SecondaryText.copy(alpha = 0.3f))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = GolhaColors.SecondaryText, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ArtistFavoriteItem(singer: SingerListItemUiModel, onClick: (Long) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick(singer.artistId) },
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        border = BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ArtistAvatar(name = singer.name, imageUrl = singer.imageUrl, tint = GolhaColors.SoftBlue, modifier = Modifier.size(48.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(singer.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText)
                Text("${singer.programCount} برنامه", style = MaterialTheme.typography.bodySmall, color = GolhaColors.SecondaryText)
            }
            GolhaLineIcon(icon = GolhaIcon.FavoritesFilled, modifier = Modifier.size(18.dp), tint = GolhaColors.PrimaryAccent)
        }
    }
}

@Composable
private fun MusicianFavoriteItem(musician: MusicianListItemUiModel, onClick: (Long) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick(musician.artistId) },
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        border = BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ArtistAvatar(name = musician.name, imageUrl = musician.imageUrl, tint = GolhaColors.SoftRose, modifier = Modifier.size(48.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(musician.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText)
                Text(musician.instrument ?: "نوازنده", style = MaterialTheme.typography.bodySmall, color = GolhaColors.SecondaryText)
            }
            GolhaLineIcon(icon = GolhaIcon.FavoritesFilled, modifier = Modifier.size(18.dp), tint = GolhaColors.PrimaryAccent)
        }
    }
}

@Composable
private fun AboutAppSection(onTap: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onTap() },
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        border = BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.5f)),
        shadowElevation = GolhaElevation.Card,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(GolhaColors.BadgeBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    GolhaLineIcon(
                        icon = GolhaIcon.Account,
                        modifier = Modifier.size(24.dp),
                        tint = GolhaColors.PrimaryText,
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        "درباره ما",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = GolhaColors.PrimaryText,
                    )
                    Text(
                        "آرشیو شنیداری برنامه‌های ماندگار گل‌ها",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GolhaColors.SecondaryText,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = GolhaColors.SoftSand.copy(alpha = 0.42f),
                border = BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.35f)),
            ) {
                Text(
                    text = "رادیو گل‌ها مجموعه‌ای از برنامه‌های گل‌های رنگارنگ رادیو ایران را با تجربه‌ای ساده، مرتب و امروزی کنار هم آورده است.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = GolhaColors.PrimaryText.copy(alpha = 0.88f),
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AboutMetaChip(label = "نسخه ۰.۱.۰")
                AboutMetaChip(label = "آرشیو شنیداری")
            }
        }
    }
}

@Composable
private fun AboutMetaChip(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = GolhaColors.SoftBlue.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.4f)),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = GolhaColors.PrimaryText,
        )
    }
}

@Composable
private fun DebugSection(
    isDebugDatabaseToolsEnabled: Boolean,
    isImportingDatabase: Boolean,
    onImportDebugDatabase: () -> Unit
) {
    Surface(
        modifier = Modifier.padding(top = 8.dp),
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        tonalElevation = 0.dp,
        shadowElevation = GolhaElevation.Card,
        border = BorderStroke(1.dp, GolhaColors.Border),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(text = "ابزار توسعه", style = MaterialTheme.typography.titleLarge, color = GolhaColors.PrimaryText)
            SmallPrimaryButton(
                label = "دریافت دیتابیس جدید",
                enabled = !isImportingDatabase,
                loading = isImportingDatabase,
                onClick = onImportDebugDatabase,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaylistAccountItem(
    playlist: SavedPlaylistUiModel,
    onClick: (Long) -> Unit,
    onLongClick: (Long) -> Unit,
) {
    val inf = rememberInfiniteTransition(label = "playlistGlow")
    val glowA by inf.animateFloat(0.04f, 0.10f, infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "plA")
    val glowR by inf.animateFloat(0.35f, 0.50f, infiniteRepeatable(tween(3500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "plR")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .combinedClickable(
                onClick = { onClick(playlist.id) },
                onLongClick = { onLongClick(playlist.id) }
            ),
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.BannerBackground,
        shadowElevation = GolhaElevation.Card,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(GolhaColors.BannerDetail.copy(alpha = glowA), size.minDimension * glowR, Offset(size.width * 0.9f, size.height * 0.4f))
                    drawCircle(GolhaColors.BannerDetail.copy(alpha = glowA * 0.6f), size.minDimension * glowR * 0.7f, Offset(size.width * 0.1f, size.height * 0.8f))
                }
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GolhaLineIcon(icon = GolhaIcon.Library, modifier = Modifier.size(16.dp), tint = GolhaColors.BannerDetail.copy(alpha = 0.6f))
                        Text(
                            text = "لیست پخش",
                            style = MaterialTheme.typography.labelSmall,
                            color = GolhaColors.BannerDetail.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GolhaColors.BannerDetail,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = GolhaColors.BannerDetail.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        GolhaLineIcon(icon = GolhaIcon.Back, modifier = Modifier.size(18.dp), tint = GolhaColors.BannerDetail)
                    }
                }
            }
        }
    }
}
