package com.radiogolha.mobile.ui.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.radiogolha.mobile.theme.*
import com.radiogolha.mobile.ui.home.*

@Composable
fun SettingsScreen(
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    favoriteSingers: List<SingerListItemUiModel> = emptyList(),
    favoriteMusicians: List<MusicianListItemUiModel> = emptyList(),
    onArtistClick: (Long) -> Unit = {},
    onShowAllFavorites: () -> Unit = {},
    onOpenDebug: () -> Unit = {},
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
    onTrackClick: (Long) -> Unit = {},
    onPlayTrack: (TrackUiModel) -> Unit = {},
    onTrackLongClick: (TrackUiModel) -> Unit = {},
    onPlaylistClick: (Long) -> Unit = {},
    onPlaylistLongClick: (Long) -> Unit = {},
) {
    var aboutTapCount by rememberSaveable { mutableIntStateOf(0) }
    var isDebugToolsVisible by rememberSaveable { mutableStateOf(false) }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("علاقه‌مندی‌ها", "لیست پخش", "اخیر", "محبوب", "درباره")

    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Rtl) {
        GolhaPatternBackground {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize(),
                containerColor = GolhaColors.ScreenBackground.copy(alpha = 0f),
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
                        style = MaterialTheme.typography.headlineLarge,
                        color = GolhaColors.PrimaryText,
                        modifier = Modifier.padding(horizontal = GolhaSpacing.ScreenHorizontal, vertical = 20.dp)
                    )

                    // Tab Selector
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = GolhaSpacing.ScreenHorizontal)
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = GolhaColors.Surface.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            tabs.forEachIndexed { index, title ->
                                val selected = selectedTabIndex == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) GolhaColors.PrimaryAccent else Color.Transparent)
                                        .clickable { selectedTabIndex = index },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp),
                                        color = if (selected) Color.White else GolhaColors.SecondaryText
                                    )
                                }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = GolhaSpacing.ScreenHorizontal,
                            end = GolhaSpacing.ScreenHorizontal,
                            bottom = innerPadding.calculateBottomPadding() + 22.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        when (selectedTabIndex) {
                            0 -> { // Favorites
                                if (favoriteSingers.isEmpty() && favoriteMusicians.isEmpty()) {
                                    item {
                                        EmptyStatePlaceholder(text = "هنوز هنرمندی را به علاقه‌مندی‌ها اضافه نکرده‌اید.")
                                    }
                                }
                                
                                if (favoriteSingers.isNotEmpty()) {
                                    item {
                                        Text(text = "خوانندگان مورد علاقه", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText, modifier = Modifier.padding(bottom = 6.dp))
                                    }
                                    items(favoriteSingers) { singer ->
                                        ArtistFavoriteItem(singer, onArtistClick)
                                    }
                                }

                                if (favoriteMusicians.isNotEmpty()) {
                                    item {
                                        Text(text = "نوازندگان مورد علاقه", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText, modifier = Modifier.padding(vertical = 6.dp))
                                    }
                                    items(favoriteMusicians) { musician ->
                                        MusicianFavoriteItem(musician, onArtistClick)
                                    }
                                }
                            }
                            1 -> { // Playlists
                                if (savedPlaylists.isEmpty()) {
                                    item {
                                        EmptyStatePlaceholder(text = "هنوز لیست پخشی نساخته‌اید.")
                                    }
                                } else {
                                    items(savedPlaylists) { playlist ->
                                        PlaylistAccountItem(playlist, onPlaylistClick, onPlaylistLongClick)
                                    }
                                }
                            }
                            2 -> { // Recently Played (Recent)
                                if (recentlyPlayedTracks.isEmpty()) {
                                    item {
                                        EmptyStatePlaceholder(text = "تاریخچه پخش شما خالی است.")
                                    }
                                } else {
                                    item {
                                        CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(GolhaColors.Surface, RoundedCornerShape(GolhaRadius.Card))
                                                    .border(1.dp, GolhaColors.Border.copy(alpha = 0.6f), RoundedCornerShape(GolhaRadius.Card))
                                                    .padding(vertical = 8.dp),
                                            ) {
                                                recentlyPlayedTracks.forEachIndexed { index, track ->
                                                    val isActive = currentTrack?.id == track.id
                                                    com.radiogolha.mobile.ui.programs.ProgramTrackRow(
                                                        track = track,
                                                        onTrackClick = { onTrackClick(track.id) },
                                                        onPlayClick = { onPlayTrack(track) },
                                                        onLongClick = { onTrackLongClick(track) },
                                                        onArtistClick = { id -> onArtistClick(id) },
                                                        isActive = isActive,
                                                        isPlaying = isActive && isPlayerPlaying,
                                                    )
                                                    if (index != recentlyPlayedTracks.lastIndex) {
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
                            }
                            3 -> { // Most Played (Popular)
                                if (mostPlayedTracks.isEmpty()) {
                                    item {
                                        EmptyStatePlaceholder(text = "هنوز آهنگ محبوب خاصی ندارید.")
                                    }
                                } else {
                                    item {
                                        CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(GolhaColors.Surface, RoundedCornerShape(GolhaRadius.Card))
                                                    .border(1.dp, GolhaColors.Border.copy(alpha = 0.6f), RoundedCornerShape(GolhaRadius.Card))
                                                    .padding(vertical = 8.dp),
                                            ) {
                                                mostPlayedTracks.forEachIndexed { index, track ->
                                                    val isActive = currentTrack?.id == track.id
                                                    com.radiogolha.mobile.ui.programs.ProgramTrackRow(
                                                        track = track,
                                                        onTrackClick = { onTrackClick(track.id) },
                                                        onPlayClick = { onPlayTrack(track) },
                                                        onLongClick = { onTrackLongClick(track) },
                                                        onArtistClick = { id -> onArtistClick(id) },
                                                        isActive = isActive,
                                                        isPlaying = isActive && isPlayerPlaying,
                                                    )
                                                    if (index != mostPlayedTracks.lastIndex) {
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
                            }
                            4 -> { // About
                                item { AboutAppSection(onTap = {
                                    aboutTapCount++
                                    if (aboutTapCount == 2) {
                                        com.radiogolha.mobile.debug.showDebugToast("یک کلیک دیگر تا فعال‌سازی حالت توسعه‌دهنده")
                                    } else if (aboutTapCount >= 3) {
                                        isDebugToolsVisible = true
                                        onOpenDebug()
                                    }
                                }) }
                                
                                item {
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = isDebugToolsVisible && isDebugDatabaseToolsEnabled,
                                        enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                                        exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                                    ) {
                                        Surface(
                                            modifier = Modifier.padding(top = 8.dp),
                                            shape = RoundedCornerShape(GolhaRadius.Card),
                                            color = GolhaColors.Surface,
                                            tonalElevation = 0.dp,
                                            shadowElevation = GolhaElevation.Card,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
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
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.5f)),
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
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.5f)),
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
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.5f)),
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
                border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.35f)),
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
        color = com.radiogolha.mobile.theme.GolhaColors.SoftBlue.copy(alpha = 0.45f),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.radiogolha.mobile.theme.GolhaColors.Border.copy(alpha = 0.4f)),
    ) {
        Text(
            text = label,
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = com.radiogolha.mobile.theme.GolhaColors.PrimaryText,
        )
    }
}
@Composable
private fun DebugDatabaseCard(
    isImportingDatabase: Boolean,
    onImportDebugDatabase: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        tonalElevation = 0.dp,
        shadowElevation = GolhaElevation.Card,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "ابزار توسعه",
                style = MaterialTheme.typography.titleLarge,
                color = GolhaColors.PrimaryText,
            )

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
    val glowA by inf.animateFloat(0.04f, 0.10f, infiniteRepeatable(tween(3000, easing = androidx.compose.animation.core.FastOutSlowInEasing), androidx.compose.animation.core.RepeatMode.Reverse), label = "plA")
    val glowR by inf.animateFloat(0.35f, 0.50f, infiniteRepeatable(tween(3500, easing = androidx.compose.animation.core.FastOutSlowInEasing), androidx.compose.animation.core.RepeatMode.Reverse), label = "plR")

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
